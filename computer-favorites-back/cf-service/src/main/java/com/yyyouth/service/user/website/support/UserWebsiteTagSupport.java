package com.yyyouth.service.user.website.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.vo.user.UserWebsiteTagItemVO;
import com.yyyouth.service.mapper.website.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-06
 *
 * 用户端网站标签转换支持组件
 */
@Component
@RequiredArgsConstructor
public class UserWebsiteTagSupport {

    private static final int NOT_DELETED = 0;

    private static final String DEFAULT_TAG_COLOR = "#409EFF";

    private final TagMapper tagMapper;

    /**
     * 构建标签映射
     *
     * @param tagIds 标签ID集合
     * @return 标签映射
     */
    public Map<Long, UserWebsiteTagItemVO> buildTagItemMap(Set<Long> tagIds) {
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptyMap();
        }

        List<Tag> tagList = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .in(Tag::getId, tagIds)
                .eq(Tag::getDeleted, NOT_DELETED));
        if (CollUtil.isEmpty(tagList)) {
            return Collections.emptyMap();
        }

        return tagList.stream().collect(Collectors.toMap(
                Tag::getId,
                tag -> {
                    UserWebsiteTagItemVO tagItemVO = BeanUtil.copyProperties(tag, UserWebsiteTagItemVO.class);
                    tagItemVO.setColor(normalizeTagColor(tag.getColor()));
                    return tagItemVO;
                },
                (left, right) -> left
        ));
    }

    /**
     * 构建标签映射
     *
     * @param tagIds 标签ID列表
     * @return 标签映射
     */
    public Map<Long, UserWebsiteTagItemVO> buildTagItemMap(List<Long> tagIds) {
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptyMap();
        }
        return buildTagItemMap(tagIds.stream().collect(Collectors.toSet()));
    }

    /**
     * 构建网站标签列表
     *
     * @param tagsRaw 网站标签原始文本
     * @param tagItemMap 标签映射
     * @return 网站标签列表
     */
    public List<UserWebsiteTagItemVO> buildWebsiteTagItems(String tagsRaw, Map<Long, UserWebsiteTagItemVO> tagItemMap) {
        List<Long> tagIds = parseTagIds(tagsRaw);
        if (CollUtil.isEmpty(tagIds) || CollUtil.isEmpty(tagItemMap)) {
            return Collections.emptyList();
        }

        return tagIds.stream()
                .map(tagItemMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 解析标签ID列表
     *
     * @param tags 标签原始文本
     * @return 标签ID列表
     */
    public List<Long> parseTagIds(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Collections.emptyList();
        }

        return Arrays.stream(tags.replace('，', ',').split(","))
                .map(tag -> tag.replaceAll("\\s+", ""))
                .filter(StringUtils::hasText)
                .map(this::parseLongSafely)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 解析Long类型
     *
     * @param text 文本
     * @return Long值
     */
    private Long parseLongSafely(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 标准化标签颜色
     *
     * @param color 原始颜色
     * @return 标准化颜色
     */
    private String normalizeTagColor(String color) {
        if (!StringUtils.hasText(color)) {
            return DEFAULT_TAG_COLOR;
        }

        String normalizedColor = color.trim().toUpperCase();
        if (!normalizedColor.startsWith("#")) {
            normalizedColor = "#" + normalizedColor;
        }
        if (!normalizedColor.matches("^#[0-9A-F]{6}$")) {
            return DEFAULT_TAG_COLOR;
        }
        return normalizedColor;
    }
}
