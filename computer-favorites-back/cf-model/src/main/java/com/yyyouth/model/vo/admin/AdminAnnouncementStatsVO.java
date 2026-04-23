package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-22
 *
 * 管理端公告统计
 */
@Data
public class AdminAnnouncementStatsVO {

    /**
     * 总数
     */
    private Long total;

    /**
     * 显示中数量
     */
    private Long visible;

    /**
     * 已隐藏数量
     */
    private Long hidden;

    /**
     * 置顶数量
     */
    private Long top;
}
