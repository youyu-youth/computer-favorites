package com.yyyouth.model.enums;

import lombok.Getter;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 举报状态枚举
 */
@Getter
public enum ReportStatus {

    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 已处理
     */
    PROCESSED(1, "已处理"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回");

    private final Integer code;
    private final String description;

    ReportStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ReportStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReportStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
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
