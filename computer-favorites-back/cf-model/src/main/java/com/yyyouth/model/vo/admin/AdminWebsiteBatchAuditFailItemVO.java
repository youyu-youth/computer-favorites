package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 管理端批量审核失败项
 */
@Data
public class AdminWebsiteBatchAuditFailItemVO {

    /**
     * 网站ID
     */
    private Long websiteId;

    /**
     * 失败原因
     */
    private String reason;
}
