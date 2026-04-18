package com.yyyouth.model.vo.admin;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈详情
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminFeedbackDetailVO extends AdminFeedbackListItemVO {

    /**
     * 回复内容
     */
    private String reply;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 详情摘要
     */
    private String summaryText;

    /**
     * 时间线
     */
    private List<AdminFeedbackTimelineItemVO> timeline;
}
