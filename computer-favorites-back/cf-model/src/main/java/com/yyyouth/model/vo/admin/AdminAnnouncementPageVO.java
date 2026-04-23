package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-22
 *
 * 管理端公告分页结果
 */
@Data
public class AdminAnnouncementPageVO {

    /**
     * 列表数据
     */
    private List<AdminAnnouncementListItemVO> records;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Long totalPages;

    /**
     * 统计信息
     */
    private AdminAnnouncementStatsVO stats;
}
