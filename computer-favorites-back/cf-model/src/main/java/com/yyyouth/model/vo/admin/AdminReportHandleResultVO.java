package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报处置结果
 */
@Data
public class AdminReportHandleResultVO {

    private Long reportId;

    private Integer status;

    private String handleResult;

    private LocalDateTime handleTime;

    private Boolean actionExecuted;
}
