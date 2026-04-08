package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息批量已读参数
 */
@Data
public class UserMessageBatchReadDTO {

    /**
     * 消息ID列表
     */
    @NotEmpty(message = "消息ID列表不能为空")
    @Size(max = 100, message = "单次批量操作最多100条")
    private List<@NotNull(message = "消息ID不能为空") @Positive(message = "消息ID必须为正数") Long> ids;
}
