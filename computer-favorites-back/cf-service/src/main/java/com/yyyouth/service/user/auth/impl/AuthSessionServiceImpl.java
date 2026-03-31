package com.yyyouth.service.user.auth.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.model.pojo.auth.UserSession;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.service.user.auth.AuthSessionService;
import com.yyyouth.service.mapper.user.auth.UserSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * 认证会话服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthSessionServiceImpl implements AuthSessionService {

    private static final int SESSION_VALID = 1;

    private static final int SESSION_INVALID = 0;

    private static final long DEFAULT_RENEW_TIMEOUT_SECONDS = 3600L;

    private final UserSessionMapper userSessionMapper;

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
     * 续期时更新会话数据
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
     */
    private LocalDateTime calculateExpireTime(long timeoutSeconds, LocalDateTime baseTime) {
        if (timeoutSeconds <= 0) {
            return null;
        }
        return baseTime.plusSeconds(timeoutSeconds);
    }

    /**
     * 获取当前会话设备类型
     */
    private String getCurrentDeviceType() {
        Object deviceType = StpUtil.getTokenSession().get("deviceType");
        if (deviceType == null) {
            return "unknown";
        }
        return String.valueOf(deviceType);
    }
}
