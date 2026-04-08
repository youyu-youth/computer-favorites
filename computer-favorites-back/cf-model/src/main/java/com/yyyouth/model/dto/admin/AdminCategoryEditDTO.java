package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类编辑参数
 */
@Data
public class AdminCategoryEditDTO {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    private String name;

    /**
     * 分类图标
     */
    @Size(max = 255, message = "分类图标长度不能超过255")
    private String icon;

    /**
     * 分类描述
     */
    @Size(max = 255, message = "分类描述长度不能超过255")
    private String description;

    /**
     * 父分类ID，0为根节点
     */
    @NotNull(message = "父分类ID不能为空")
    @Min(value = 0, message = "父分类ID不能小于0")
    private Long parentId = 0L;

    /**
     * 排序值
     */
    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort = 0;

    /**
     * 状态：0禁用，1启用
     */
    @NotNull(message = "分类状态不能为空")
    @Min(value = 0, message = "分类状态参数不合法")
    @Max(value = 1, message = "分类状态参数不合法")
    private Integer status;
}
