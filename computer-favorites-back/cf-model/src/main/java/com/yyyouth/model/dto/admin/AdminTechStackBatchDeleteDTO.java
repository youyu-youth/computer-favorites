package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端批量删除技术栈参数
 */
@Data
public class AdminTechStackBatchDeleteDTO {

    /**
     * 技术栈ID列表
     */
    @NotEmpty(message = "技术栈ID列表不能为空")
    @Size(max = 200, message = "单次最多删除200个技术栈")
    private List<@NotNull(message = "技术栈ID不能为空") @Positive(message = "技术栈ID不合法") Long> techStackIds;
}
