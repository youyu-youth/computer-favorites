package com.yyyouth.model.enums;

import lombok.Getter;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 举报类型枚举
 */
@Getter
public enum ReportType {

    /**
     * 网站
     */
    WEBSITE(1, "网站"),

    /**
     * 评论
     */
    COMMENT(2, "评论");

    private final Integer code;
    private final String description;

    ReportType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ReportType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReportType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 校验code是否有效
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
