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
 * 用户端网站投稿工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserWebsiteTool {

    @Tool(description = "创建网站投稿草稿。用户提供网站信息后生成草稿，待用户确认后正式提交审核")
    public String submitWebsiteDraft(
            @ToolParam(description = "网站名称") String name,
            @ToolParam(description = "网站URL地址") String url,
            @ToolParam(description = "网站简短简介（一句话）") String summary,
            @ToolParam(description = "网站详细描述") String description,
            @ToolParam(description = "分类ID") Long categoryId,
            @ToolParam(description = "标签，逗号分隔") String tags) {

        log.info("UserWebsiteTool.submitWebsiteDraft: name={}, url={}, categoryId={}", name, url, categoryId);

        return String.format(
                "网站投稿草稿已生成：\n名称：%s\nURL：%s\n简介：%s\n分类ID：%d\n标签：%s\n请确认是否提交审核。",
                name, url, summary, categoryId, tags);
    }
}
