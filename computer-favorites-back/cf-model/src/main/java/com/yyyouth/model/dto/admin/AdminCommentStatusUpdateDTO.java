package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端单条评论状态变更参数
 */
@Data
public class AdminCommentStatusUpdateDTO {

    /**
     * 目标状态：0隐藏，1显示
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态参数不合法")
    @Max(value = 1, message = "状态参数不合法")
    private Integer status;

    /**
     * 操作原因
     */
    @Size(max = 200, message = "操作原因长度不能超过200")
    private String reason;
}
