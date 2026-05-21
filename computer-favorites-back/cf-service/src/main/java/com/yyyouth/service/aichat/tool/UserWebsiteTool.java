package com.yyyouth.service.aichat.tool;

import cn.hutool.core.util.StrUtil;
import com.yyyouth.model.dto.agent.ToolResult;
import com.yyyouth.model.dto.agent.WebsiteDraftDTO;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionCreateDTO;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.service.aichat.chat.AgentRequestContext;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.TagMapper;
import com.yyyouth.service.user.website.UserWebsiteSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 用户端网站投稿工具。接受单 DTO（全字段可选），
 * 参数不全时返回 incomplete JSON 由前端展示补全卡片，
 * 参数齐全时执行真实提交。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserWebsiteTool {

    private final UserWebsiteSubmissionService submissionService;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    /** 投稿必填字段。summary 由后端从 description 自动截取，icon 自动从 URL 推断。 */
    private static final Set<String> REQUIRED_FIELDS = Set.of("name", "url", "description", "categoryId");

    @Tool(name = "user_submit_website", description = "提交网站投稿。用户提供网站信息后创建投稿记录，待管理员审核")
    public String submitWebsiteDraft(
            @ToolParam(description = "网站投稿信息，尽可能从对话中提取已提供的信息，不确定的字段可留空")
            WebsiteDraftDTO dto) {

        log.info("UserWebsiteTool.submitWebsiteDraft: name={}, url={}", dto.getName(), dto.getUrl());

        // 1. 收集已填字段
        Map<String, Object> collected = new LinkedHashMap<>();
        if (StrUtil.isNotBlank(dto.getName())) collected.put("name", dto.getName());
        if (StrUtil.isNotBlank(dto.getUrl())) collected.put("url", dto.getUrl());
        if (StrUtil.isNotBlank(dto.getDescription())) collected.put("description", dto.getDescription());
        if (dto.getCategoryId() != null && dto.getCategoryId() > 0) collected.put("categoryId", dto.getCategoryId());
        if (StrUtil.isNotBlank(dto.getTags())) collected.put("tags", dto.getTags());
        // icon 和 summary 由后端自动推断，不展示在 collected 中

        // 2. 检测必填字段缺失
        List<ToolResult.MissingField> missing = new ArrayList<>();

        if (StrUtil.isBlank(dto.getName())) {
            missing.add(ToolResult.MissingField.builder()
                    .field("name").label("网站名称").type("text").required(true).build());
        }
        if (StrUtil.isBlank(dto.getUrl())) {
            missing.add(ToolResult.MissingField.builder()
                    .field("url").label("网站URL（http://或https://开头）").type("text").required(true).build());
        }
        if (StrUtil.isBlank(dto.getDescription())) {
            missing.add(ToolResult.MissingField.builder()
                    .field("description").label("详细描述").type("text").required(true).build());
        }
        if (dto.getCategoryId() == null || dto.getCategoryId() <= 0) {
            List<ToolResult.Suggestion> catSuggestions = new ArrayList<>();
            try {
                List<WebsiteCategory> categories = categoryMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WebsiteCategory>()
                                .eq(WebsiteCategory::getStatus, 1)
                                .eq(WebsiteCategory::getDeleted, 0)
                                .orderByAsc(WebsiteCategory::getSort));
                for (WebsiteCategory cat : categories) {
                    catSuggestions.add(ToolResult.Suggestion.builder()
                            .value(cat.getId()).label(cat.getName()).build());
                }
            } catch (Exception e) {
                log.warn("查询分类列表失败", e);
            }
            missing.add(ToolResult.MissingField.builder()
                    .field("categoryId").label("分类").type("select").required(true)
                    .suggestions(catSuggestions).build());
        }
        if (StrUtil.isBlank(dto.getTags())) {
            List<ToolResult.Suggestion> tagSuggestions = new ArrayList<>();
            try {
                List<Tag> tags = tagMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                                .eq(Tag::getDeleted, 0)
                                .orderByDesc(Tag::getUseCount)
                                .last("LIMIT 10"));
                for (Tag tag : tags) {
                    tagSuggestions.add(ToolResult.Suggestion.builder()
                            .value(tag.getName()).label(tag.getName() + "（" + tag.getUseCount() + "次）").build());
                }
            } catch (Exception e) {
                log.warn("查询标签列表失败", e);
            }
            missing.add(ToolResult.MissingField.builder()
                    .field("tags").label("标签（逗号分隔）").type("text").required(false)
                    .suggestions(tagSuggestions).build());
        }

        if (!missing.isEmpty()) {
            log.info("UserWebsiteTool: 参数不全, collected={}, missing={}", collected.size(), missing.size());
            return ToolResult.incomplete(collected, missing,
                    "已收到 " + collected.size() + " 项信息，还需补充 " + missing.size() + " 项").toJson();
        }

        // 3. 参数齐全 → 执行提交
        UserWebsiteSubmissionCreateDTO createDTO = new UserWebsiteSubmissionCreateDTO();
        createDTO.setName(dto.getName());
        createDTO.setUrl(dto.getUrl());
        createDTO.setIcon(dto.getUrl().replaceAll("/$", "") + "/favicon.ico");
        // summary 从 description 自动截取前 200 字
        createDTO.setSummary(StrUtil.sub(dto.getDescription(), 0, 200));
        createDTO.setDescription(dto.getDescription());
        createDTO.setCategoryId(dto.getCategoryId());
        createDTO.setTags(dto.getTags());

        Long submitterId = AgentRequestContext.getCurrentUserId();
        if (submitterId == null) {
            log.error("AgentRequestContext.getCurrentUserId() 返回 null，无法提交投稿");
            return ToolResult.builder()
                    .status("incomplete")
                    .message("系统错误：无法获取当前用户信息，请刷新页面后重试")
                    .build().toJson();
        }
        Long websiteId = submissionService.submitWebsite(createDTO, submitterId);
        log.info("网站投稿成功: websiteId={}, name={}", websiteId, dto.getName());

        return ToolResult.submitted(websiteId, dto.getName(), dto.getUrl()).toJson();
    }
}
