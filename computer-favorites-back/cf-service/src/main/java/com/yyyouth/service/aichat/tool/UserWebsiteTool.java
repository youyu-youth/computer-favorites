package com.yyyouth.service.aichat.tool;

import com.yyyouth.model.dto.user.UserWebsiteSubmissionCreateDTO;
import com.yyyouth.service.user.website.UserWebsiteSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 用户端网站投稿工具。LLM 收集网站信息后调用此工具写入投稿草稿。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserWebsiteTool {

    private final UserWebsiteSubmissionService submissionService;

    @Tool(name = "user_submit_website", description = "提交网站投稿。用户提供网站信息后创建投稿记录，待管理员审核")
    public String submitWebsiteDraft(
            @ToolParam(description = "网站名称") String name,
            @ToolParam(description = "网站URL地址（必须以http://或https://开头）") String url,
            @ToolParam(description = "网站简短简介（一句话，不超过200字）") String summary,
            @ToolParam(description = "网站详细描述") String description,
            @ToolParam(description = "分类ID") Long categoryId,
            @ToolParam(description = "标签名称，多个用逗号分隔") String tags) {

        log.info("UserWebsiteTool.submitWebsiteDraft: name={}, url={}, categoryId={}", name, url, categoryId);

        UserWebsiteSubmissionCreateDTO dto = new UserWebsiteSubmissionCreateDTO();
        dto.setName(name);
        dto.setUrl(url);
        dto.setIcon(url + "/favicon.ico");
        dto.setSummary(summary != null ? summary : "");
        dto.setDescription(description != null ? description : "");
        dto.setCategoryId(categoryId);
        dto.setTags(tags);

        Long websiteId = submissionService.submitWebsite(dto);
        log.info("网站投稿成功: websiteId={}, name={}", websiteId, name);

        return String.format("网站投稿已提交成功！\n网站ID：%d\n名称：%s\nURL：%s\n请等待管理员审核。", websiteId, name, url);
    }
}
