package com.yyyouth.model.vo.user;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息批量已读结果
 */
@Data
public class UserMessageBatchReadResultVO {

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 失败消息ID列表
     */
    private List<Long> failedIds;
}
