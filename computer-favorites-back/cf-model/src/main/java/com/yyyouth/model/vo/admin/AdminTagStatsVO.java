package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-03
 *
 * 管理端标签统计
 */
@Data
public class AdminTagStatsVO {

    /**
     * 标签总数
     */
    private Long total;

    /**
     * 已使用数量
     */
    private Long inUse;

    /**
     * 未使用数量
     */
    private Long unused;

    /**
     * 今日更新数量
     */
    private Long updatedToday;
}
