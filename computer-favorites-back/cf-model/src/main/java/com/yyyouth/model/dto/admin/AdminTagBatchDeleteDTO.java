package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-03
 *
 * 管理端批量删除标签参数
 */
@Data
public class AdminTagBatchDeleteDTO {

    /**
     * 标签ID列表
     */
    @NotEmpty(message = "标签ID列表不能为空")
    @Size(max = 200, message = "单次最多删除200个标签")
    private List<@NotNull(message = "标签ID不能为空") @Positive(message = "标签ID不合法") Long> tagIds;
}
