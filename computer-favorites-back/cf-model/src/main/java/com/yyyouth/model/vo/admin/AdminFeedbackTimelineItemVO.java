package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈时间线节点
 */
@Data
public class AdminFeedbackTimelineItemVO {

    private String id;

    private String title;

    private String description;

    private String time;

    private String tone;
}
