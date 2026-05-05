package com.yyyouth.service.admin.comment;

import com.yyyouth.model.dto.admin.AdminCommentBatchStatusUpdateDTO;
import com.yyyouth.model.dto.admin.AdminCommentQueryDTO;
import com.yyyouth.model.dto.admin.AdminCommentStatusUpdateDTO;
import com.yyyouth.model.vo.admin.AdminCommentBatchStatusResultVO;
import com.yyyouth.model.vo.admin.AdminCommentDetailVO;
import com.yyyouth.model.vo.admin.AdminCommentPageVO;
import com.yyyouth.model.vo.admin.AdminCommentStatisticsVO;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论管理服务
 */
public interface AdminCommentService {

    /**
     * 分页查询评论列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    AdminCommentPageVO queryCommentPage(AdminCommentQueryDTO queryDTO);

    /**
     * 查询评论统计概览
     *
     * @return 统计数据
     */
    AdminCommentStatisticsVO queryCommentStatistics();

    /**
     * 查询评论详情（含回复列表）
     *
     * @param commentId 评论ID
     * @return 评论详情
     */
    AdminCommentDetailVO queryCommentDetail(Long commentId);

    /**
     * 单条评论状态变更（隐藏/显示）
     *
     * @param commentId 评论ID
     * @param statusDTO 状态变更参数
     */
    void updateCommentStatus(Long commentId, AdminCommentStatusUpdateDTO statusDTO);

    /**
     * 批量评论状态变更（隐藏/显示）
     *
     * @param batchDTO 批量状态变更参数
     * @return 批量操作结果
     */
    AdminCommentBatchStatusResultVO batchUpdateCommentStatus(AdminCommentBatchStatusUpdateDTO batchDTO);
}
