package com.yyyouth.service.admin.feedback;

import com.yyyouth.model.dto.admin.AdminFeedbackQueryDTO;
import com.yyyouth.model.dto.admin.AdminFeedbackReplyDTO;
import com.yyyouth.model.vo.admin.AdminFeedbackDetailVO;
import com.yyyouth.model.vo.admin.AdminFeedbackHandleResultVO;
import com.yyyouth.model.vo.admin.AdminFeedbackPageVO;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈处理服务接口
 */
public interface AdminFeedbackService {

    /**
     * 查询反馈分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    AdminFeedbackPageVO queryFeedbackPage(AdminFeedbackQueryDTO queryDTO);

    /**
     * 查询反馈详情
     *
     * @param feedbackId 反馈ID
     * @return 反馈详情
     */
    AdminFeedbackDetailVO queryFeedbackDetail(Long feedbackId);

    /**
     * 回复反馈
     *
     * @param feedbackId 反馈ID
     * @param replyDTO 回复参数
     * @return 处理结果
     */
    AdminFeedbackHandleResultVO replyFeedback(Long feedbackId, AdminFeedbackReplyDTO replyDTO);

    /**
     * 关闭反馈
     *
     * @param feedbackId 反馈ID
     * @return 处理结果
     */
    AdminFeedbackHandleResultVO closeFeedback(Long feedbackId);
}
