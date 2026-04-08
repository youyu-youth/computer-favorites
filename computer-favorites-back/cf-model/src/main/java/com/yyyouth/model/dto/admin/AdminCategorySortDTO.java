package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类排序更新参数
 */
@Data
public class AdminCategorySortDTO {

    /**
     * 排序值
     */
    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;
}
