package com.yyyouth.service.auth.support;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员登录体系工具
 */
public final class StpAdminUtil {

    private static final StpLogic STP_LOGIC = new StpLogic("admin");

    private StpAdminUtil() {
    }

    /**
     * 管理员登录
     *
     * @param loginId 管理员ID
     * @param loginParameter 登录参数
     */
    public static void login(Long loginId, SaLoginParameter loginParameter) {
        STP_LOGIC.login(loginId, loginParameter);
    }

    /**
     * 校验管理员登录状态
     */
    public static void checkLogin() {
        STP_LOGIC.checkLogin();
    }

    /**
     * 获取管理员登录ID
     *
     * @return 管理员ID
     */
    public static Long getLoginIdAsLong() {
        return STP_LOGIC.getLoginIdAsLong();
    }

    /**
     * 获取当前管理员Token
     *
     * @return Token值
     */
    public static String getTokenValue() {
        return STP_LOGIC.getTokenValue();
    }

    /**
     * 获取当前管理员Token名称
     *
     * @return Token名称
     */
    public static String getTokenName() {
        return STP_LOGIC.getTokenName();
    }

    /**
     * 获取当前管理员Token剩余有效秒数
     *
     * @return 剩余秒数
     */
    public static long getTokenTimeout() {
        return STP_LOGIC.getTokenTimeout();
    }

    /**
     * 获取管理员Token会话
     *
     * @return Token会话
     */
    public static SaSession getTokenSession() {
        return STP_LOGIC.getTokenSession();
    }

    /**
     * 续期管理员Token
     *
     * @param timeoutSeconds 续期秒数
     */
    public static void renewTimeout(long timeoutSeconds) {
        STP_LOGIC.renewTimeout(timeoutSeconds);
    }

    /**
     * 刷新最后活跃时间
     */
    public static void updateLastActiveToNow() {
        STP_LOGIC.updateLastActiveToNow();
    }

    /**
     * 管理员退出登录
     */
    public static void logout() {
        STP_LOGIC.logout();
    }
}
