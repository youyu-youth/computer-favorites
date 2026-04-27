package com.yyyouth.service.common;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-04-27
 *
 * 认证上下文工具类
 * 统一封装当前用户ID获取逻辑，兼容本地开发环境关闭鉴权的场景
 */
@Component
public class AuthContext {

    private final boolean authEnable;

    public AuthContext(@Value("${cf.auth.enable:true}") boolean authEnable) {
        this.authEnable = authEnable;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public Long getCurrentUserId() {
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginIdAsLong();
        }
        if (!authEnable) {
            return 1L;
        }
        StpUtil.checkLogin();
        return StpUtil.getLoginIdAsLong();
    }
}
