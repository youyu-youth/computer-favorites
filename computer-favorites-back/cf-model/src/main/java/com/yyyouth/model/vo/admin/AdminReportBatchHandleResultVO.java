package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端批量举报处置结果
 */
@Data
public class AdminReportBatchHandleResultVO {

    private Integer total;

    private Integer success;

    private Integer failed;

    private List<AdminReportBatchHandleResultItemVO> results;
}
