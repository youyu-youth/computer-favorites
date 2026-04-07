package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 管理端批量审核参数
 */
@Data
public class AdminWebsiteBatchAuditDTO {

    /**
     * 网站ID列表
     */
    @NotEmpty(message = "网站ID列表不能为空")
    @Size(max = 100, message = "单次批量审核最多100条")
    private List<@NotNull(message = "网站ID不能为空") @Positive(message = "网站ID必须为正数") Long> websiteIds;

    /**
     * 审核动作：1通过，2拒绝
     */
    @NotNull(message = "审核动作不能为空")
    @Min(value = 1, message = "审核动作不合法")
    @Max(value = 2, message = "审核动作不合法")
    private Integer action;

    /**
     * 审核备注（批量拒绝时必填）
     */
    @Size(max = 500, message = "审核备注长度不能超过500")
    private String remark;
}
