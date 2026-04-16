package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端批量举报处置结果项
 */
@Data
public class AdminReportBatchHandleResultItemVO {

    private Long reportId;

    private Boolean success;

    private String message;
}
