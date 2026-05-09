package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页隐私设置更新参数（user-15 M5）。
 * 对应 PUT /api/user/profile/privacy。
 *
 * 字段：
 *  - profileVisibility：主页可见性 public / logged / private
 *  - showContribution：是否对他人展示贡献热力图与贡献分（0/1）
 *  - showCollections：是否对他人展示收藏列表（0/1）
 */
@Data
public class ProfilePrivacyUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "主页可见性不能为空")
    @Pattern(regexp = "public|logged|private", message = "主页可见性仅允许 public/logged/private")
    private String profileVisibility;

    @NotNull(message = "贡献可见性开关不能为空")
    @Min(value = 0, message = "贡献可见性仅允许 0 或 1")
    @Max(value = 1, message = "贡献可见性仅允许 0 或 1")
    private Integer showContribution;

    @NotNull(message = "收藏可见性开关不能为空")
    @Min(value = 0, message = "收藏可见性仅允许 0 或 1")
    @Max(value = 1, message = "收藏可见性仅允许 0 或 1")
    private Integer showCollections;
}
