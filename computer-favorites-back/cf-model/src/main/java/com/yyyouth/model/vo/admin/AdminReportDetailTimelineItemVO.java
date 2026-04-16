package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报详情时间线节点
 */
@Data
public class AdminReportDetailTimelineItemVO {

    private String id;

    private String title;

    private String description;

    private String time;

    private String tone;
}
