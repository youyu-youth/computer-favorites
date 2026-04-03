package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-03
 *
 * 管理端编辑标签参数
 */
@Data
public class AdminTagEditDTO {

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50")
    private String name;

    /**
     * 标签颜色
     */
    @NotBlank(message = "标签颜色不能为空")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "标签颜色格式应为 #RRGGBB")
    private String color;
}
