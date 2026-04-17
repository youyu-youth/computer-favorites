package com.yyyouth.model.enums;

import lombok.Getter;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈类型枚举
 */
@Getter
public enum FeedbackType {

    /**
     * 建议
     */
    SUGGESTION(1, "建议"),

    /**
     * Bug 反馈
     */
    BUG(2, "Bug反馈"),

    /**
     * 投诉
     */
    COMPLAINT(3, "投诉"),

    /**
     * 使用感受
     */
    EXPERIENCE(4, "使用感受");

    private final Integer code;
    private final String description;

    FeedbackType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取类型枚举
     *
     * @param code 类型编码
     * @return 类型枚举
     */
    public static FeedbackType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FeedbackType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 校验编码是否合法
     *
     * @param code 类型编码
     * @return 是否合法
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
