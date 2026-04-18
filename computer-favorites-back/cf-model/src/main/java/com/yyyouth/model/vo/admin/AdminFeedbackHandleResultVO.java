package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈处理结果
 */
@Data
public class AdminFeedbackHandleResultVO {

    private Long feedbackId;

    private Integer status;

    private String reply;

    private LocalDateTime replyTime;

    private LocalDateTime updateTime;
}
