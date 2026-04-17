package com.yyyouth.model.enums;

import lombok.Getter;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 用户站内消息类型枚举
 */
@Getter
public enum UserMessageType {

    /**
     * 系统通知
     */
    SYSTEM(1, "系统通知"),

    /**
     * 评论回复
     */
    COMMENT_REPLY(2, "评论回复"),

    /**
     * 收藏提醒
     */
    FAVORITE_REMINDER(3, "收藏提醒"),

    /**
     * 审核结果
     */
    AUDIT_RESULT(4, "审核结果"),

    /**
     * 举报反馈
     */
    REPORT_FEEDBACK(5, "举报反馈");

    private final Integer code;
    private final String description;

    UserMessageType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取消息类型
     *
     * @param code 类型编码
     * @return 消息类型枚举
     */
    public static UserMessageType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserMessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 校验消息类型编码是否合法
     *
     * @param code 类型编码
     * @return 是否合法
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
