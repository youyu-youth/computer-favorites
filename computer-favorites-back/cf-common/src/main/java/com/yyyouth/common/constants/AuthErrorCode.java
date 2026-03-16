package com.yyyouth.common.constants;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 认证模块错误码定义
 */
public enum AuthErrorCode {

    /**
     * 用户名或密码错误
     */
    INVALID_CREDENTIAL(100201, "账号或密码错误"),

    /**
     * 当前会话未登录
     */
    UNAUTHORIZED(100202, "登录状态已失效"),

    /**
     * 当前用户无权限访问
     */
    FORBIDDEN(100203, "无权限访问该资源"),

    /**
     * 用户不存在或已禁用
     */
    USER_DISABLED(100204, "用户不存在或已被禁用");

    private final int code;

    private final String message;

    AuthErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
