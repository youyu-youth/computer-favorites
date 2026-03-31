package com.yyyouth.service.admin.impl;

import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.service.admin.AdminSessionService;
import com.yyyouth.service.auth.support.StpAdminUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员会话服务实现
 */
@Service
public class AdminSessionServiceImpl implements AdminSessionService {

    private static final long DEFAULT_RENEW_TIMEOUT_SECONDS = 3600L;

    /**
     * 续期当前管理员会话
     */
    @Override
    public void renewSession() {
        StpAdminUtil.checkLogin();
        long currentTimeout = StpAdminUtil.getTokenTimeout();
        long renewTimeout = currentTimeout > 0 ? currentTimeout : DEFAULT_RENEW_TIMEOUT_SECONDS;
        StpAdminUtil.renewTimeout(renewTimeout);
        StpAdminUtil.updateLastActiveToNow();
    }

    /**
     * 退出当前管理员会话
     */
    @Override
    public void logout() {
        StpAdminUtil.checkLogin();
        StpAdminUtil.logout();
    }

    /**
     * 查询当前管理员会话
     *
     * @return 当前会话
     */
    @Override
    public AuthSessionVO currentSession() {
        StpAdminUtil.checkLogin();
        Long adminId = StpAdminUtil.getLoginIdAsLong();
        String tokenValue = StpAdminUtil.getTokenValue();
        long timeoutSeconds = StpAdminUtil.getTokenTimeout();
        LocalDateTime expireTime = calculateExpireTime(timeoutSeconds, LocalDateTime.now());

        AuthSessionVO sessionVO = new AuthSessionVO();
        sessionVO.setUserId(adminId);
        sessionVO.setTokenValue(tokenValue);
        sessionVO.setDeviceType(getCurrentDeviceType());
        sessionVO.setTimeoutSeconds(timeoutSeconds);
        sessionVO.setExpireTime(expireTime);
        return sessionVO;
    }

    /**
     * 获取当前会话设备类型
     */
    private String getCurrentDeviceType() {
        Object deviceType = StpAdminUtil.getTokenSession().get("deviceType");
        if (deviceType == null) {
            return "unknown";
        }
        return String.valueOf(deviceType);
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
}
