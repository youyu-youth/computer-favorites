package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 管理端网站审核参数
 */
@Data
public class AdminWebsiteAuditDTO {

    /**
     * 审核动作：1通过，2拒绝
     */
    @NotNull(message = "审核动作不能为空")
    @Min(value = 1, message = "审核动作不合法")
    @Max(value = 2, message = "审核动作不合法")
    private Integer action;

    /**
     * 审核备注（拒绝时必填）
     */
    @Size(max = 500, message = "审核备注长度不能超过500")
    private String remark;
}
