package com.yyyouth.model.enums;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页可见性枚举（user-15 M5）。
 *
 * 对应 t_user_setting.profile_visibility，三档语义见 spec §4.4：
 *  - PUBLIC：所有人（含未登录访客）可见
 *  - LOGGED：仅登录用户可见，未登录访问返回 PROFILE_LOGIN_REQUIRED
 *  - PRIVATE：仅本人可见，他人访问返回 PROFILE_PRIVATE（不暴露用户存在性）
 */
public enum ProfileVisibility {

    PUBLIC("public"),
    LOGGED("logged"),
    PRIVATE("private");

    private final String code;

    ProfileVisibility(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 安全解析：null / 未知值一律按 PUBLIC（最宽松）兜底，避免开关异常导致主页彻底不可访问。
     */
    public static ProfileVisibility fromCode(String code) {
        if (code == null) {
            return PUBLIC;
        }
        for (ProfileVisibility v : values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        return PUBLIC;
    }
}
