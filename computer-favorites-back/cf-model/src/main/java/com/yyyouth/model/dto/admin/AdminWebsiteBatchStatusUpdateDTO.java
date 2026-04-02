package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-02
 *
 * 管理端网站批量状态更新参数
 */
@Data
public class AdminWebsiteBatchStatusUpdateDTO {

    /**
     * 网站ID列表
     */
    @NotEmpty(message = "网站ID列表不能为空")
    private List<@NotNull(message = "网站ID不能为空") Long> websiteIds;

    /**
     * 目标状态：0下架，1上架
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态参数不合法")
    @Max(value = 1, message = "状态参数不合法")
    private Integer status;
}