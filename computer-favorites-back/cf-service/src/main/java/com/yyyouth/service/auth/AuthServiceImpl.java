package com.yyyouth.service.auth;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.auth.UserSession;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.model.vo.auth.AuthUserVO;
import com.yyyouth.service.auth.impl.AuthService;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.auth.UserSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
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

    private final UserAccountMapper userAccountMapper;

    private final UserSessionMapper userSessionMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        if (!StringUtils.hasText(encodedPassword) || !passwordEncoder.matches(loginDTO.getPassword(), encodedPassword)) {
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
        writeSessionToRedis(userAccount.getId(), tokenValue, loginDTO.getDeviceType(), timeoutSeconds, expireTime);

        AuthUserVO authUserVO = new AuthUserVO();
        authUserVO.setUserId(userAccount.getId());
        authUserVO.setUsername(userAccount.getUsername());
        authUserVO.setNickname(userAccount.getNickname());

        AuthLoginVO authLoginVO = new AuthLoginVO();
        authLoginVO.setTokenValue(tokenValue);
        authLoginVO.setTokenName(StpUtil.getTokenName());
        authLoginVO.setExpireTime(expireTime);
        authLoginVO.setUserInfo(authUserVO);
        return authLoginVO;
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
        writeSessionToRedis(userId, tokenValue, getCurrentDeviceType(), renewTimeout, expireTime);
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

        stringRedisTemplate.delete(RedisConstant.AUTH_SESSION_TOKEN + tokenValue);
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
     * 写入会话Redis缓存
     *
     * @param userId 用户ID
     * @param tokenValue token值
     * @param deviceType 设备类型
     * @param timeoutSeconds 超时秒数
     * @param expireTime 过期时间
     */
    private void writeSessionToRedis(Long userId, String tokenValue, String deviceType, long timeoutSeconds, LocalDateTime expireTime) {
        AuthSessionVO sessionVO = new AuthSessionVO();
        sessionVO.setUserId(userId);
        sessionVO.setTokenValue(tokenValue);
        sessionVO.setDeviceType(deviceType);
        sessionVO.setTimeoutSeconds(timeoutSeconds);
        sessionVO.setExpireTime(expireTime);

        String key = RedisConstant.AUTH_SESSION_TOKEN + tokenValue;
        String value = JSONUtil.toJsonStr(sessionVO);
        if (timeoutSeconds > 0) {
            stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeoutSeconds));
            return;
        }
        stringRedisTemplate.opsForValue().set(key, value);
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
}
