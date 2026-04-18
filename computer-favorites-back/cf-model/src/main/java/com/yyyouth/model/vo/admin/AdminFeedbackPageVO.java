package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈分页结果
 */
@Data
public class AdminFeedbackPageVO {

    /**
     * 列表数据
     */
    private List<AdminFeedbackListItemVO> records;

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
     * 当前筛选条件下的统计摘要
     */
    private AdminFeedbackStatisticsVO statistics;
}
