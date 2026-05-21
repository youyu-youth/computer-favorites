package com.yyyouth.service.aichat.tool;

import cn.hutool.json.JSONUtil;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * 数据查询工具集。提供分类/标签等参考数据的查询，
 * LLM 可在对话中主动调用以获取真实数据库中的数据。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLookupTool {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Tool(name = "list_categories",
          description = "查询所有可用的网站分类列表，返回分类ID和名称。投稿或浏览网站时可用来获取分类选项")
    public String listCategories() {
        List<WebsiteCategory> list = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WebsiteCategory>()
                        .eq(WebsiteCategory::getStatus, 1)
                        .eq(WebsiteCategory::getDeleted, 0)
                        .orderByAsc(WebsiteCategory::getSort));

        List<Map<String, Object>> result = new ArrayList<>();
        for (WebsiteCategory cat : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", cat.getId());
            item.put("name", cat.getName());
            if (cat.getDescription() != null) {
                item.put("description", cat.getDescription());
            }
            result.add(item);
        }
        log.info("list_categories: 返回 {} 个分类", result.size());
        return JSONUtil.toJsonStr(result);
    }

    @Tool(name = "list_tags",
          description = "查询热门标签列表，返回标签名称和使用次数。投稿或搜索网站时可使用")
    public String listTags() {
        List<Tag> list = tagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                        .eq(Tag::getDeleted, 0)
                        .orderByDesc(Tag::getUseCount)
                        .last("LIMIT 30"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tag tag : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            item.put("useCount", tag.getUseCount());
            result.add(item);
        }
        log.info("list_tags: 返回 {} 个标签", result.size());
        return JSONUtil.toJsonStr(result);
    }
}
