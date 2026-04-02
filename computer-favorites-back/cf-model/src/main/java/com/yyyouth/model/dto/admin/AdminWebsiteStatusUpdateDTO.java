package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-02
 *
 * 管理端网站状态更新参数
 */
@Data
public class AdminWebsiteStatusUpdateDTO {

    /**
     * 网站ID
     */
    @NotNull(message = "网站ID不能为空")
    private Long websiteId;

    /**
     * 目标状态：0下架，1上架
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态参数不合法")
    @Max(value = 1, message = "状态参数不合法")
    private Integer status;
}