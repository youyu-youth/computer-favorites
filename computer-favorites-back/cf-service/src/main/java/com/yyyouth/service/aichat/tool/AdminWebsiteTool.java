package com.yyyouth.service.aichat.tool;

import com.yyyouth.model.dto.admin.AdminWebsiteAuditDTO;
import com.yyyouth.service.admin.website.AdminWebsiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 管理端网站审核工具。高风险操作，执行前需经 ConfirmableToolCallback 确认。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminWebsiteTool {

    private final AdminWebsiteService adminWebsiteService;

    @Tool(name = "admin_audit_website", description = "审核单个网站投稿。审核动作为1通过2拒绝。此操作为高风险，执行前需要管理员确认")
    public String auditWebsite(
            @ToolParam(description = "网站ID") Long websiteId,
            @ToolParam(description = "审核动作：1表示通过，2表示拒绝") Integer auditAction,
            @ToolParam(description = "审核备注/理由") String remark) {

        log.info("AdminWebsiteTool.auditWebsite: websiteId={}, action={}, remark={}", websiteId, auditAction, remark);

        AdminWebsiteAuditDTO dto = new AdminWebsiteAuditDTO();
        dto.setAction(auditAction);
        dto.setRemark(remark != null ? remark : "");

        adminWebsiteService.auditWebsite(websiteId, dto);

        String statusText = auditAction == 1 ? "通过" : "拒绝";
        return String.format("网站审核完成：ID=%d，结果=%s，备注=%s", websiteId, statusText, remark);
    }

    @Tool(name = "admin_batch_audit_website", description = "批量审核多个网站投稿。参数为JSON数组，每项包含websiteId和auditAction")
    public String batchAuditWebsite(
            @ToolParam(description = "批量审核数据，JSON数组格式：[{\"websiteId\":1,\"action\":1,\"remark\":\"\"}]") String batchJson) {

        log.info("AdminWebsiteTool.batchAuditWebsite: batchJson={}", batchJson);

        try {
            cn.hutool.json.JSONArray array = cn.hutool.json.JSONUtil.parseArray(batchJson);
            int successCount = 0;
            for (int i = 0; i < array.size(); i++) {
                cn.hutool.json.JSONObject item = array.getJSONObject(i);
                Long websiteId = item.getLong("websiteId");
                Integer action = item.getInt("action");
                String remark = item.getStr("remark");

                AdminWebsiteAuditDTO dto = new AdminWebsiteAuditDTO();
                dto.setAction(action);
                dto.setRemark(remark != null ? remark : "");
                adminWebsiteService.auditWebsite(websiteId, dto);
                successCount++;
            }
            return String.format("批量审核完成：共处理 %d 个网站", successCount);
        } catch (Exception e) {
            log.error("批量审核解析失败", e);
            return "批量审核失败：JSON 解析错误 - " + e.getMessage();
        }
    }
}
