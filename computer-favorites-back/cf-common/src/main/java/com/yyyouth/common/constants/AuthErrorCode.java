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
    USER_DISABLED(100204, "用户不存在或已被禁用"),

    /**
     * 注册邮箱已存在
     */
    REGISTER_EMAIL_EXISTS(100205, "该邮箱已被注册"),

    /**
     * 注册用户名已存在
     */
    REGISTER_USERNAME_EXISTS(100206, "该用户名已被使用"),

    /**
     * 注册验证码已过期
     */
    REGISTER_EMAIL_CODE_EXPIRED(100207, "验证码已过期，请重新获取"),

    /**
     * 注册验证码错误
     */
    REGISTER_EMAIL_CODE_INVALID(100208, "验证码不正确"),

    /**
     * 验证码发送过于频繁
     */
    REGISTER_EMAIL_CODE_SEND_TOO_FAST(100209, "验证码发送过于频繁，请稍后重试"),

    /**
     * 验证码发送次数超限
     */
    REGISTER_EMAIL_CODE_SEND_LIMIT(100210, "今日验证码发送次数已达上限"),

    /**
     * 验证码发送失败
     */
    REGISTER_EMAIL_CODE_SEND_FAILED(100211, "验证码发送失败，请稍后重试"),

    /**
     * 登录验证码已过期
     */
    LOGIN_EMAIL_CODE_EXPIRED(100212, "登录验证码已过期，请重新获取"),

    /**
     * 登录验证码错误
     */
    LOGIN_EMAIL_CODE_INVALID(100213, "登录验证码不正确"),

    /**
     * 登录验证码发送过于频繁
     */
    LOGIN_EMAIL_CODE_SEND_TOO_FAST(100214, "登录验证码发送过于频繁，请稍后重试"),

    /**
     * 登录验证码发送次数超限
     */
    LOGIN_EMAIL_CODE_SEND_LIMIT(100215, "今日登录验证码发送次数已达上限"),

    /**
     * 登录验证码发送失败
     */
    LOGIN_EMAIL_CODE_SEND_FAILED(100216, "登录验证码发送失败，请稍后重试"),

    /**
     * 修改密码验证码已过期
     */
    RESET_EMAIL_CODE_EXPIRED(100217, "验证码已过期，请重新获取"),

    /**
     * 修改密码验证码错误
     */
    RESET_EMAIL_CODE_INVALID(100218, "验证码不正确"),

    /**
     * 修改密码验证码发送过于频繁
     */
    RESET_EMAIL_CODE_SEND_TOO_FAST(100219, "验证码发送过于频繁，请稍后重试"),

    /**
     * 修改密码验证码发送次数超限
     */
    RESET_EMAIL_CODE_SEND_LIMIT(100220, "今日验证码发送次数已达上限"),

    /**
     * 修改密码验证码发送失败
     */
    RESET_EMAIL_CODE_SEND_FAILED(100221, "验证码发送失败，请稍后重试"),

    /**
     * 当前密码错误
     */
    CHANGE_PASSWORD_CURRENT_INVALID(100222, "当前密码不正确"),

    /**
     * 新密码与确认密码不一致
     */
    CHANGE_PASSWORD_CONFIRM_MISMATCH(100223, "两次输入的新密码不一致"),

    /**
     * 新密码强度不足
     */
    CHANGE_PASSWORD_STRENGTH_INVALID(100224, "新密码强度不足，请包含字母、数字和特殊字符"),

    /**
     * 绑定邮箱不匹配
     */
    CHANGE_PASSWORD_EMAIL_MISMATCH(100225, "邮箱与当前账号不匹配"),

    /**
     * 新密码不能与当前密码相同
     */
    CHANGE_PASSWORD_SAME_AS_OLD(100226, "新密码不能与当前密码相同"),

    /**
     * 修改密码失败
     */
    CHANGE_PASSWORD_UPDATE_FAILED(100227, "修改密码失败，请稍后重试"),

    /**
     * 新用户名已被使用
     */
    USERNAME_ALREADY_EXISTS(100228, "该用户名已被使用"),

    /**
     * 新用户名与当前用户名相同
     */
    USERNAME_SAME_AS_OLD(100229, "新用户名不能与当前用户名相同"),

    /**
     * 用户名包含敏感词
     */
    USERNAME_CONTAINS_SENSITIVE_WORD(100230, "用户名包含敏感词，请重新输入"),

    /**
     * 用户名每月只能修改一次
     */
    USERNAME_UPDATE_MONTHLY_LIMIT(100231, "用户名每月仅允许修改一次"),

    /**
     * 用户名更新失败
     */
    USERNAME_UPDATE_FAILED(100232, "用户名修改失败，请稍后重试");

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
