package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报统计数据
 */
@Data
public class AdminReportStatisticsVO {

    private Integer total;

    private Integer pending;

    private Integer processed;

    private Integer rejected;

    private Integer last24Hours;

    private Integer websiteCount;

    private Integer commentCount;

    private Integer processRate;
}
