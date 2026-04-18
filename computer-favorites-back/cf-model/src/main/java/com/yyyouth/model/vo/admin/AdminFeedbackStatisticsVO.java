package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈统计数据
 */
@Data
public class AdminFeedbackStatisticsVO {

    private Integer total;

    private Integer pending;

    private Integer processed;

    private Integer closed;

    private Integer last24Hours;

    private Integer bugCount;

    private Integer withImagesCount;

    private Integer withContactCount;

    private Integer replyRate;
}
