package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-01
 *
 * 管理端网站统计信息
 */
@Data
public class AdminWebsiteStatsVO {

    /**
     * 网站总数
     */
    private Long total;

    /**
     * 上架数量
     */
    private Long online;

    /**
     * 下架数量
     */
    private Long offline;

    /**
     * 待审核数量
     */
    private Long pendingAudit;

    /**
     * 审核拒绝数量
     */
    private Long rejectedAudit;

    /**
     * 已删除数量
     */
    private Long deleted;

    /**
     * 最新更新时间
     */
    private LocalDateTime latestUpdateTime;
}
