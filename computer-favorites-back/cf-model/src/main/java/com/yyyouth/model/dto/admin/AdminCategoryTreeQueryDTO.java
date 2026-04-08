package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类树查询参数
 */
@Data
public class AdminCategoryTreeQueryDTO {

    /**
     * 分类状态：0禁用，1启用
     */
    @Min(value = 0, message = "分类状态参数不合法")
    @Max(value = 1, message = "分类状态参数不合法")
    private Integer status;
}
