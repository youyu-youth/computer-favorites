package com.yyyouth.model.enums;

import lombok.Getter;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈状态枚举
 */
@Getter
public enum FeedbackStatus {

    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 已处理
     */
    PROCESSED(1, "已处理"),

    /**
     * 已关闭
     */
    CLOSED(2, "已关闭");

    private final Integer code;
    private final String description;

    FeedbackStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取状态枚举
     *
     * @param code 状态编码
     * @return 状态枚举
     */
    public static FeedbackStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FeedbackStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 校验编码是否合法
     *
     * @param code 状态编码
     * @return 是否合法
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
