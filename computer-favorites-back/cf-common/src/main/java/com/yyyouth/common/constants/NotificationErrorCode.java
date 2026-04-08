package com.yyyouth.common.constants;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 通知中心错误码定义
 */
public enum NotificationErrorCode {

    /**
     * 消息不存在
     */
    MESSAGE_NOT_FOUND(42001, "消息不存在"),

    /**
     * 消息归属校验失败
     */
    MESSAGE_OWNER_MISMATCH(42002, "消息归属校验失败"),

    /**
     * 反馈提交失败
     */
    FEEDBACK_SUBMIT_FAILED(42003, "反馈提交失败"),

    /**
     * 批量消息已读参数为空
     */
    MESSAGE_BATCH_EMPTY(42004, "批量消息ID不能为空"),

    /**
     * 批量消息已读超出限制
     */
    MESSAGE_BATCH_LIMIT_EXCEEDED(42005, "单次批量操作最多100条"),

    /**
     * 公告不存在
     */
    ANNOUNCEMENT_NOT_FOUND(42006, "公告不存在"),

    /**
     * 公告不可访问
     */
    ANNOUNCEMENT_NOT_VISIBLE(42007, "公告不可访问");

    private final int code;

    private final String message;

    NotificationErrorCode(int code, String message) {
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
