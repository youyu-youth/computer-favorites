package com.yyyouth.model.vo.admin;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报详情
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminReportDetailVO extends AdminReportListItemVO {

    /**
     * 目标状态标签
     */
    private String targetStatusLabel;

    /**
     * 证据摘要
     */
    private String evidenceSummary;

    /**
     * 时间线
     */
    private List<AdminReportDetailTimelineItemVO> timeline;
}
