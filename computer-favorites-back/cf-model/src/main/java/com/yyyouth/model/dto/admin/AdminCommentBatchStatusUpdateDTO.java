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
 * @date 2026-05-04
 *
 * 管理端批量评论状态变更参数
 */
@Data
public class AdminCommentBatchStatusUpdateDTO {

    /**
     * 评论ID列表
     */
    @NotEmpty(message = "评论ID列表不能为空")
    @Size(max = 50, message = "单次批量操作最多50条")
    private List<@NotNull(message = "评论ID不能为空") @Positive(message = "评论ID必须为正数") Long> ids;

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
