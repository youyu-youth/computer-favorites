package com.yyyouth.service.aichat.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 管理端网站审核工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminWebsiteTool {

    @Tool(description = "审核单个网站投稿，审核结果为1通过2拒绝")
    public String auditWebsite(
            @ToolParam(description = "网站ID") Long websiteId,
            @ToolParam(description = "审核结果：1表示通过，2表示拒绝") Integer auditStatus,
            @ToolParam(description = "审核备注/理由") String remark) {

        log.info("AdminWebsiteTool.auditWebsite: websiteId={}, auditStatus={}, remark={}", websiteId, auditStatus, remark);

        String statusText = auditStatus == 1 ? "通过" : "拒绝";
        return String.format("网站审核结果：ID=%d，结果=%s，备注=%s。请确认执行此审核操作。",
                websiteId, statusText, remark);
    }

    @Tool(description = "批量审核多个网站投稿")
    public String batchAuditWebsite(
            @ToolParam(description = "批量审核数据，JSON数组，每项包含websiteId和auditStatus") String batchJson) {

        log.info("AdminWebsiteTool.batchAuditWebsite: batchJson={}", batchJson);

        return String.format("批量审核数据已解析：%s。请确认是否批量执行审核。", batchJson);
    }
}
