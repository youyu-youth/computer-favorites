package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论统计信息
 */
@Data
public class AdminCommentStatisticsVO {

    /**
     * 评论总数
     */
    private Integer total;

    /**
     * 今日新增
     */
    private Integer todayNew;

    /**
     * 显示数量
     */
    private Integer visible;

    /**
     * 隐藏数量
     */
    private Integer hidden;
}
