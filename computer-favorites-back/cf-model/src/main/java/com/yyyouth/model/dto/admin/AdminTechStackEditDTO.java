package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端编辑技术栈参数
 */
@Data
public class AdminTechStackEditDTO {

    /**
     * 技术栈名称
     */
    @NotBlank(message = "技术栈名称不能为空")
    @Size(max = 100, message = "技术栈名称长度不能超过100")
    private String name;

    /**
     * PNG图标地址
     */
    @Size(max = 500, message = "图标地址长度不能超过500")
    private String iconPng;

    /**
     * 官网地址
     */
    @Size(max = 500, message = "官网地址长度不能超过500")
    private String officialUrl;

    /**
     * 技术栈描述
     */
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;

    /**
     * 主题色（十六进制）
     */
    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "主题色格式应为 #RRGGBB")
    private String color;

    /**
     * 排序值
     */
    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    /**
     * 状态：0禁用，1正常
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态参数不合法")
    @Max(value = 1, message = "状态参数不合法")
    private Integer status;
}
