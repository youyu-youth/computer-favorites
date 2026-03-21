package com.yyyouth.service.auth.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.CaptchaConstants;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.utils.EmailUtils;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.dto.auth.AuthLoginEmailCodeDTO;
import com.yyyouth.model.dto.auth.AuthRegisterDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.auth.UserSession;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.model.vo.auth.AuthUserVO;
import com.yyyouth.service.auth.AuthService;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.auth.UserSessionMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int ENABLED_STATUS = 1;

    private static final int SESSION_VALID = 1;

    private static final int SESSION_INVALID = 0;

    private static final long DEFAULT_RENEW_TIMEOUT_SECONDS = 3600L;

    private static final int UNVERIFIED = 0;

    private static final int VERIFIED = 1;

    private static final int NOT_DELETED = 0;

    private static final String DEFAULT_THEME = "dark";

    private static final String DEFAULT_LANGUAGE = "zh-CN";

    private static final String DEFAULT_HOMEPAGE_STYLE = "grid";

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserAccountMapper userAccountMapper;

    private final UserSessionMapper userSessionMapper;

    private final UserProfileMapper userProfileMapper;

    private final UserSettingMapper userSettingMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final EmailUtils emailUtils;

    /**
     * 用户登录并建立会话
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @Override
    public AuthLoginVO login(AuthLoginDTO loginDTO) {
        log.info("登录：loginDTO: {}", JSONUtil.toJsonStr(loginDTO));
        String loginIdentity = loginDTO.getUsername();
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getDeleted, 0)
                .and(wrapper -> wrapper.eq(UserAccount::getUsername, loginIdentity)
                        .or()
                        .eq(UserAccount::getEmail, loginIdentity))
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIAL.getCode(), AuthErrorCode.INVALID_CREDENTIAL.getMessage());
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(userAccount.getStatus())) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }
        String encodedPassword = resolveEncodedPassword(userAccount);
        if (!StringUtils.hasText(encodedPassword) || !PASSWORD_ENCODER.matches(loginDTO.getPassword(), encodedPassword)) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIAL.getCode(), AuthErrorCode.INVALID_CREDENTIAL.getMessage());
        }
        return doLogin(userAccount, loginDTO.getDeviceType(), "login");
    }

    /**
     * 邮箱验证码登录并建立会话
     *
     * @param loginEmailCodeDTO 登录参数
     * @return 登录结果
     */
    @Override
    public AuthLoginVO loginByEmailCode(AuthLoginEmailCodeDTO loginEmailCodeDTO) {
        String normalizedEmail = normalizeEmail(loginEmailCodeDTO.getEmail());
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getEmail, normalizedEmail)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIAL.getCode(), AuthErrorCode.INVALID_CREDENTIAL.getMessage());
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(userAccount.getStatus())) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }
        validateLoginEmailCode(normalizedEmail, loginEmailCodeDTO.getEmailCode());
        AuthLoginVO authLoginVO = doLogin(userAccount, loginEmailCodeDTO.getDeviceType(), "login_by_email_code");
        stringRedisTemplate.delete(buildLoginCodeKey(normalizedEmail));
        return authLoginVO;
    }

    /**
     * 注册账号
     *
     * @param registerDTO 注册参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(AuthRegisterDTO registerDTO) {
        String normalizedEmail = normalizeEmail(registerDTO.getEmail());
        String normalizedUsername = normalizeUsername(registerDTO.getUsername());

        UserAccount existAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getEmail, normalizedEmail)
                .last("limit 1"));
        if (existAccount != null) {
            throw new BusinessException(AuthErrorCode.REGISTER_EMAIL_EXISTS.getCode(), AuthErrorCode.REGISTER_EMAIL_EXISTS.getMessage());
        }
        if (!checkUsernameAvailable(normalizedUsername)) {
            throw new BusinessException(AuthErrorCode.REGISTER_USERNAME_EXISTS.getCode(), AuthErrorCode.REGISTER_USERNAME_EXISTS.getMessage());
        }
        validateRegisterEmailCode(normalizedEmail, registerDTO.getEmailCode());

        LocalDateTime now = LocalDateTime.now();
        UserAccount userAccount = buildRegisterUserAccount(registerDTO, normalizedEmail, normalizedUsername, now);
        userAccountMapper.insert(userAccount);
        initUserProfile(userAccount.getId(), now);
        initUserSetting(userAccount.getId(), now);
        stringRedisTemplate.delete(buildRegisterCodeKey(normalizedEmail));
    }

    /**
     * 发送注册邮箱验证码
     *
     * @param email 注册邮箱
     */
    @Override
    public void sendRegisterEmailCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        enforceRegisterCodeSendPolicy(normalizedEmail);
        String code = RandomUtil.randomNumbers(6);
        boolean sendSuccess;
        try {
            sendSuccess = emailUtils.sendGeneralEmail(
                    CaptchaConstants.CAPTCHA_TITLE,
                    "您的注册验证码为：" + code + "，" + CaptchaConstants.CODE_EXPIRE_MINUTES + "分钟内有效。",
                    normalizedEmail
            );
        } catch (Exception ex) {
            log.error("发送注册验证码失败，email={}", normalizedEmail, ex);
            throw new BusinessException(AuthErrorCode.REGISTER_EMAIL_CODE_SEND_FAILED.getCode(), AuthErrorCode.REGISTER_EMAIL_CODE_SEND_FAILED.getMessage());
        }
        if (!sendSuccess) {
            throw new BusinessException(AuthErrorCode.REGISTER_EMAIL_CODE_SEND_FAILED.getCode(), AuthErrorCode.REGISTER_EMAIL_CODE_SEND_FAILED.getMessage());
        }
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(
                buildRegisterCodeKey(normalizedEmail),
                code,
                CaptchaConstants.CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );
    }

    /**
     * 发送登录邮箱验证码
     *
     * @param email 登录邮箱
     */
    @Override
    public void sendLoginEmailCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getEmail, normalizedEmail)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIAL.getCode(), AuthErrorCode.INVALID_CREDENTIAL.getMessage());
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(userAccount.getStatus())) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }
        enforceLoginCodeSendPolicy(normalizedEmail);
        String code = RandomUtil.randomNumbers(6);
        boolean sendSuccess;
        try {
            sendSuccess = emailUtils.sendGeneralEmail(
                    CaptchaConstants.CAPTCHA_TITLE,
                    "您的登录验证码为：" + code + "，" + CaptchaConstants.CODE_EXPIRE_MINUTES + "分钟内有效。",
                    normalizedEmail
            );
        } catch (Exception ex) {
            log.error("发送登录验证码失败，email={}", normalizedEmail, ex);
            throw new BusinessException(AuthErrorCode.LOGIN_EMAIL_CODE_SEND_FAILED.getCode(), AuthErrorCode.LOGIN_EMAIL_CODE_SEND_FAILED.getMessage());
        }
        if (!sendSuccess) {
            throw new BusinessException(AuthErrorCode.LOGIN_EMAIL_CODE_SEND_FAILED.getCode(), AuthErrorCode.LOGIN_EMAIL_CODE_SEND_FAILED.getMessage());
        }
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(
                buildLoginCodeKey(normalizedEmail),
                code,
                CaptchaConstants.CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );
    }

    /**
     * 校验用户名是否可用
     *
     * @param username 用户名
     * @return true可用 false不可用
     */
    @Override
    public boolean checkUsernameAvailable(String username) {
        String normalizedUsername = normalizeUsername(username);
        UserAccount existAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getUsername, normalizedUsername)
                .last("limit 1"));
        return existAccount == null;
    }

    /**
     * 续期当前会话
     */
    @Override
    public void renewSession() {
        StpUtil.checkLogin();
        String tokenValue = StpUtil.getTokenValue();
        long currentTimeout = StpUtil.getTokenTimeout();
        long renewTimeout = currentTimeout > 0 ? currentTimeout : DEFAULT_RENEW_TIMEOUT_SECONDS;

        StpUtil.renewTimeout(renewTimeout);
        StpUtil.updateLastActiveToNow();

        Long userId = StpUtil.getLoginIdAsLong();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = calculateExpireTime(renewTimeout, now);  
        updateSessionOnRenew(userId, tokenValue, now, expireTime);
    }

    /**
     * 退出当前会话
     */
    @Override
    public void logout() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        String tokenValue = StpUtil.getTokenValue();
        LocalDateTime now = LocalDateTime.now();

        StpUtil.logout();

        userSessionMapper.update(null, new LambdaUpdateWrapper<UserSession>()
                .eq(UserSession::getTokenValue, tokenValue)
                .set(UserSession::getStatus, SESSION_INVALID)
                .set(UserSession::getUpdateBy, String.valueOf(userId))
                .set(UserSession::getUpdateTime, now)
                .set(UserSession::getOperationSource, "logout"));
    }

    /**
     * 查询当前会话
     *
     * @return 会话信息
     */
    @Override
    public AuthSessionVO currentSession() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        String tokenValue = StpUtil.getTokenValue();
        long timeoutSeconds = StpUtil.getTokenTimeout();
        LocalDateTime expireTime = calculateExpireTime(timeoutSeconds, LocalDateTime.now());

        AuthSessionVO sessionVO = new AuthSessionVO();
        sessionVO.setUserId(userId);
        sessionVO.setTokenValue(tokenValue);
        sessionVO.setDeviceType(getCurrentDeviceType());
        sessionVO.setTimeoutSeconds(timeoutSeconds);
        sessionVO.setExpireTime(expireTime);
        return sessionVO;
    }

    /**
     * 执行登录并生成统一登录结果
     *
     * @param userAccount 用户账号
     * @param deviceType 设备类型
     * @param operationSource 操作来源
     * @return 登录结果
     */
    private AuthLoginVO doLogin(UserAccount userAccount, String deviceType, String operationSource) {
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(deviceType);
        StpUtil.login(userAccount.getId(), loginParameter);
        StpUtil.getTokenSession().set("deviceType", deviceType);

        String tokenValue = StpUtil.getTokenValue();
        long timeoutSeconds = StpUtil.getTokenTimeout();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = calculateExpireTime(timeoutSeconds, now);

        saveUserSession(userAccount, tokenValue, deviceType, now, expireTime, operationSource);

        AuthUserVO authUserVO = BeanUtil.copyProperties(userAccount, AuthUserVO.class);
        authUserVO.setUserId(userAccount.getId());

        AuthLoginVO authLoginVO = new AuthLoginVO();
        authLoginVO.setTokenValue(tokenValue);
        authLoginVO.setTokenName(StpUtil.getTokenName());
        authLoginVO.setExpireTime(expireTime);
        authLoginVO.setUserInfo(authUserVO);
        return authLoginVO;
    }

    /**
     * 写入用户会话表
     *
     * @param userAccount 用户账号
     * @param tokenValue Token值
     * @param deviceType 设备类型
     * @param now 当前时间
     * @param expireTime 过期时间
     * @param operationSource 操作来源
     */
    private void saveUserSession(UserAccount userAccount, String tokenValue, String deviceType, LocalDateTime now,
                                 LocalDateTime expireTime, String operationSource) {
        UserSession dbSession = userSessionMapper.selectOne(new LambdaQueryWrapper<UserSession>()
                .eq(UserSession::getTokenValue, tokenValue)
                .last("limit 1"));
        if (dbSession == null) {
            UserSession userSession = new UserSession();
            userSession.setUserId(userAccount.getId());
            userSession.setTokenValue(tokenValue);
            userSession.setDeviceType(deviceType);
            userSession.setStatus(SESSION_VALID);
            userSession.setLoginTime(now);
            userSession.setRenewTime(now);
            userSession.setExpireTime(expireTime);
            userSession.setCreateBy(String.valueOf(userAccount.getId()));
            userSession.setUpdateBy(String.valueOf(userAccount.getId()));
            userSession.setOperationSource(operationSource);
            userSession.setCreateTime(now);
            userSession.setUpdateTime(now);
            userSessionMapper.insert(userSession);
            return;
        }

        userSessionMapper.update(null, new LambdaUpdateWrapper<UserSession>()
                .eq(UserSession::getTokenValue, tokenValue)
                .set(UserSession::getStatus, SESSION_VALID)
                .set(UserSession::getRenewTime, now)
                .set(UserSession::getExpireTime, expireTime)
                .set(UserSession::getUpdateBy, String.valueOf(userAccount.getId()))
                .set(UserSession::getUpdateTime, now)
                .set(UserSession::getOperationSource, operationSource));
    }

    /**
     * 续期时更新会话数据
     *
     * @param userId 用户ID
     * @param tokenValue token值
     * @param now 当前时间
     * @param expireTime 过期时间
     */
    private void updateSessionOnRenew(Long userId, String tokenValue, LocalDateTime now, LocalDateTime expireTime) {
        userSessionMapper.update(null, new LambdaUpdateWrapper<UserSession>()
                .eq(UserSession::getTokenValue, tokenValue)
                .set(UserSession::getStatus, SESSION_VALID)
                .set(UserSession::getRenewTime, now)
                .set(UserSession::getExpireTime, expireTime)
                .set(UserSession::getUpdateBy, String.valueOf(userId))
                .set(UserSession::getUpdateTime, now)
                .set(UserSession::getOperationSource, "renew"));
    }

    /**
     * 计算过期时间
     *
     * @param timeoutSeconds 超时秒数
     * @param baseTime 基准时间
     * @return 过期时间
     */
    private LocalDateTime calculateExpireTime(long timeoutSeconds, LocalDateTime baseTime) {
        if (timeoutSeconds <= 0) {
            return null;
        }
        return baseTime.plusSeconds(timeoutSeconds);
    }

    /**
     * 获取当前会话设备类型
     *
     * @return 设备类型
     */
    private String getCurrentDeviceType() {
        Object deviceType = StpUtil.getTokenSession().get("deviceType");
        if (deviceType == null) {
            return "unknown";
        }
        return String.valueOf(deviceType);
    }

    /**
     * 解析数据库中的加密密码
     *
     * @param userAccount 用户账号
     * @return 加密密码
     */
    private String resolveEncodedPassword(UserAccount userAccount) {
        if (StringUtils.hasText(userAccount.getPasswordHash())) {
            return userAccount.getPasswordHash();
        }
        return userAccount.getPassword();
    }

    /**
     * 构建注册账号实体
     *
     * @param registerDTO 注册参数
     * @param now 当前时间
     * @return 账号实体
     */
    private UserAccount buildRegisterUserAccount(AuthRegisterDTO registerDTO, String normalizedEmail, String normalizedUsername, LocalDateTime now) {
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(normalizedEmail);
        userAccount.setUsername(normalizedUsername);
        userAccount.setNickname(buildDefaultNickname(normalizedUsername, normalizedEmail));
        userAccount.setPasswordHash(PASSWORD_ENCODER.encode(registerDTO.getPassword()));
        userAccount.setPassword(userAccount.getPasswordHash());
        userAccount.setStatus(ENABLED_STATUS);
        userAccount.setEmailVerified(VERIFIED);
        userAccount.setPhoneVerified(UNVERIFIED);
        userAccount.setDeleted(NOT_DELETED);
        userAccount.setCreateTime(now);
        userAccount.setUpdateTime(now);
        return userAccount;
    }

    /**
     * 初始化用户资料
     *
     * @param userId 用户ID
     * @param now 当前时间
     */
    private void initUserProfile(Long userId, LocalDateTime now) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setDeleted(NOT_DELETED);
        userProfile.setCreateTime(now);
        userProfile.setUpdateTime(now);
        userProfileMapper.insert(userProfile);
    }

    /**
     * 初始化用户设置
     *
     * @param userId 用户ID
     * @param now 当前时间
     */
    private void initUserSetting(Long userId, LocalDateTime now) {
        UserSetting userSetting = new UserSetting();
        userSetting.setUserId(userId);
        userSetting.setTheme(DEFAULT_THEME);
        userSetting.setLanguage(DEFAULT_LANGUAGE);
        userSetting.setEmailNotice(1);
        userSetting.setCollectNotice(1);
        userSetting.setCommentNotice(1);
        userSetting.setHomepageStyle(DEFAULT_HOMEPAGE_STYLE);
        userSetting.setPageSize(DEFAULT_PAGE_SIZE);
        userSetting.setCreateTime(now);
        userSetting.setUpdateTime(now);
        userSettingMapper.insert(userSetting);
    }

    /**
     * 根据邮箱生成默认昵称
     *
     * @param username 用户名
     * @param email 邮箱
     * @return 默认昵称
     */
    private String buildDefaultNickname(String username, String email) {
        if (StringUtils.hasText(username)) {
            return username;
        }
        String prefix = email;
        int atIndex = email.indexOf("@");
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }
        return prefix;
    }

    /**
     * 校验注册验证码
     *
     * @param email 邮箱
     * @param code 验证码
     */
    private void validateRegisterEmailCode(String email, String code) {
        String cacheCode = stringRedisTemplate.opsForValue().get(buildRegisterCodeKey(email));
        if (!StringUtils.hasText(cacheCode)) {
            throw new BusinessException(AuthErrorCode.REGISTER_EMAIL_CODE_EXPIRED.getCode(), AuthErrorCode.REGISTER_EMAIL_CODE_EXPIRED.getMessage());
        }
        if (!cacheCode.equals(code)) {
            throw new BusinessException(AuthErrorCode.REGISTER_EMAIL_CODE_INVALID.getCode(), AuthErrorCode.REGISTER_EMAIL_CODE_INVALID.getMessage());
        }
    }

    /**
     * 校验登录验证码
     *
     * @param email 邮箱
     * @param code 验证码
     */
    private void validateLoginEmailCode(String email, String code) {
        String cacheCode = stringRedisTemplate.opsForValue().get(buildLoginCodeKey(email));
        if (!StringUtils.hasText(cacheCode)) {
            throw new BusinessException(AuthErrorCode.LOGIN_EMAIL_CODE_EXPIRED.getCode(), AuthErrorCode.LOGIN_EMAIL_CODE_EXPIRED.getMessage());
        }
        if (!cacheCode.equals(code)) {
            throw new BusinessException(AuthErrorCode.LOGIN_EMAIL_CODE_INVALID.getCode(), AuthErrorCode.LOGIN_EMAIL_CODE_INVALID.getMessage());
        }
    }

    /**
     * 校验验证码发送策略
     *
     * @param email 邮箱
     */
    private void enforceRegisterCodeSendPolicy(String email) {
        String intervalKey = buildRegisterCodeIntervalKey(email);
        Boolean exists = stringRedisTemplate.hasKey(intervalKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new BusinessException(AuthErrorCode.REGISTER_EMAIL_CODE_SEND_TOO_FAST.getCode(), AuthErrorCode.REGISTER_EMAIL_CODE_SEND_TOO_FAST.getMessage());
        }

        String countKey = buildRegisterCodeCountKey(email);
        Integer sendCount = toInteger(stringRedisTemplate.opsForValue().get(countKey));
        if (sendCount != null && sendCount >= CaptchaConstants.MAX_SEND_COUNT_PER_DAY) {
            throw new BusinessException(AuthErrorCode.REGISTER_EMAIL_CODE_SEND_LIMIT.getCode(), AuthErrorCode.REGISTER_EMAIL_CODE_SEND_LIMIT.getMessage());
        }

        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.increment(countKey);
        stringRedisTemplate.expire(countKey, 1, TimeUnit.DAYS);
        valueOperations.set(
                intervalKey,
                "1",
                CaptchaConstants.SEND_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    /**
     * 校验登录验证码发送策略
     *
     * @param email 邮箱
     */
    private void enforceLoginCodeSendPolicy(String email) {
        String intervalKey = buildLoginCodeIntervalKey(email);
        Boolean exists = stringRedisTemplate.hasKey(intervalKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new BusinessException(AuthErrorCode.LOGIN_EMAIL_CODE_SEND_TOO_FAST.getCode(), AuthErrorCode.LOGIN_EMAIL_CODE_SEND_TOO_FAST.getMessage());
        }

        String countKey = buildLoginCodeCountKey(email);
        Integer sendCount = toInteger(stringRedisTemplate.opsForValue().get(countKey));
        if (sendCount != null && sendCount >= CaptchaConstants.MAX_SEND_COUNT_PER_DAY) {
            throw new BusinessException(AuthErrorCode.LOGIN_EMAIL_CODE_SEND_LIMIT.getCode(), AuthErrorCode.LOGIN_EMAIL_CODE_SEND_LIMIT.getMessage());
        }

        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.increment(countKey);
        stringRedisTemplate.expire(countKey, 1, TimeUnit.DAYS);
        valueOperations.set(
                intervalKey,
                "1",
                CaptchaConstants.SEND_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    /**
     * 规范化邮箱
     *
     * @param email 邮箱
     * @return 规范化邮箱
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化用户名
     *
     * @param username 用户名
     * @return 规范化用户名
     */
    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 构建注册验证码键
     *
     * @param email 邮箱
     * @return Redis键
     */
    private String buildRegisterCodeKey(String email) {
        return CaptchaConstants.REGISTER_CODE_KEY_PREFIX + email;
    }

    /**
     * 构建登录验证码键
     *
     * @param email 邮箱
     * @return Redis键
     */
    private String buildLoginCodeKey(String email) {
        return CaptchaConstants.LOGIN_CODE_KEY_PREFIX + email;
    }

    /**
     * 构建注册验证码发送间隔键
     *
     * @param email 邮箱
     * @return Redis键
     */
    private String buildRegisterCodeIntervalKey(String email) {
        return buildRegisterCodeKey(email) + ":interval";
    }

    /**
     * 构建登录验证码发送间隔键
     *
     * @param email 邮箱
     * @return Redis键
     */
    private String buildLoginCodeIntervalKey(String email) {
        return buildLoginCodeKey(email) + ":interval";
    }

    /**
     * 构建注册验证码发送计数键
     *
     * @param email 邮箱
     * @return Redis键
     */
    private String buildRegisterCodeCountKey(String email) {
        return buildRegisterCodeKey(email) + ":count:" + LocalDate.now();
    }

    /**
     * 构建登录验证码发送计数键
     *
     * @param email 邮箱
     * @return Redis键
     */
    private String buildLoginCodeCountKey(String email) {
        return buildLoginCodeKey(email) + ":count:" + LocalDate.now();
    }

    /**
     * 字符串转整数
     *
     * @param value 字符串值
     * @return 整数值
     */
    private Integer toInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
