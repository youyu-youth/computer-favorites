package com.yyyouth.service.auth;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.dto.auth.AuthRegisterDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.auth.UserSession;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.model.vo.auth.AuthUserVO;
import com.yyyouth.service.auth.impl.AuthService;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.auth.UserSessionMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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

        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(loginDTO.getDeviceType());
        StpUtil.login(userAccount.getId(), loginParameter);
        StpUtil.getTokenSession().set("deviceType", loginDTO.getDeviceType());

        String tokenValue = StpUtil.getTokenValue();
        long timeoutSeconds = StpUtil.getTokenTimeout();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = calculateExpireTime(timeoutSeconds, now);

        saveUserSession(userAccount, tokenValue, loginDTO.getDeviceType(), now, expireTime);

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
     * 注册账号
     *
     * @param registerDTO 注册参数
     */
    @Override
    public void register(AuthRegisterDTO registerDTO) {
        UserAccount existAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getEmail, registerDTO.getEmail())
                .last("limit 1"));
        if (existAccount != null) {
            throw new BusinessException(AuthErrorCode.REGISTER_EMAIL_EXISTS.getCode(), AuthErrorCode.REGISTER_EMAIL_EXISTS.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        UserAccount userAccount = buildRegisterUserAccount(registerDTO, now);
        userAccountMapper.insert(userAccount);
        initUserProfile(userAccount.getId(), now);
        initUserSetting(userAccount.getId(), now);
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
     * 写入用户会话表
     *
     * @param userAccount 用户账号
     * @param tokenValue Token值
     * @param deviceType 设备类型
     * @param now 当前时间
     * @param expireTime 过期时间
     */
    private void saveUserSession(UserAccount userAccount, String tokenValue, String deviceType, LocalDateTime now, LocalDateTime expireTime) {
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
            userSession.setOperationSource("login");
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
                .set(UserSession::getOperationSource, "login"));
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
    private UserAccount buildRegisterUserAccount(AuthRegisterDTO registerDTO, LocalDateTime now) {
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(registerDTO.getEmail());
        userAccount.setUsername(buildUsernameByEmail(registerDTO.getEmail()));
        userAccount.setNickname(buildDefaultNickname(registerDTO.getEmail()));
        userAccount.setPasswordHash(PASSWORD_ENCODER.encode(registerDTO.getPassword()));
        userAccount.setPassword(userAccount.getPasswordHash());
        userAccount.setStatus(ENABLED_STATUS);
        userAccount.setEmailVerified(UNVERIFIED);
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
     * 根据邮箱生成默认用户名
     *
     * @param email 邮箱
     * @return 默认用户名
     */
    private String buildUsernameByEmail(String email) {
        String prefix = email;
        int atIndex = email.indexOf("@");
        if (atIndex > 0) {
            prefix = email.substring(0, atIndex);
        }
        return "u_" + prefix + "_" + System.currentTimeMillis();
    }

    /**
     * 根据邮箱生成默认昵称
     *
     * @param email 邮箱
     * @return 默认昵称
     */
    private String buildDefaultNickname(String email) {
        String prefix = email;
        int atIndex = email.indexOf("@");
        if (atIndex > 0) {
            prefix = email.substring(0, atIndex);
        }
        return prefix;
    }
}
