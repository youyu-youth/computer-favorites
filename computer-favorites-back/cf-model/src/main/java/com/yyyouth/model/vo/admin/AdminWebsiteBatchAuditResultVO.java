package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 管理端批量审核结果
 */
@Data
public class AdminWebsiteBatchAuditResultVO {

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 失败明细
     */
    private List<AdminWebsiteBatchAuditFailItemVO> failItems;
}
