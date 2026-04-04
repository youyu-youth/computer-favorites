package com.yyyouth.service.user.website.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserWebsiteQueryDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.user.UserWebsiteCategoryVO;
import com.yyyouth.model.vo.user.UserWebsiteDetailVO;
import com.yyyouth.model.vo.user.UserWebsiteListItemVO;
import com.yyyouth.model.vo.user.UserWebsitePageVO;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.website.UserWebsiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站资源服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserWebsiteServiceImpl implements UserWebsiteService {

    private static final int NOT_DELETED = 0;

    private static final int ONLINE_STATUS = 1;

    private static final int AUDIT_APPROVED_STATUS = 1;

    private static final int CATEGORY_ENABLED_STATUS = 1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 12;

    private final WebsiteMapper websiteMapper;

    private final CategoryMapper categoryMapper;

    /**
     * 查询网站分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public UserWebsitePageVO queryWebsitePage(UserWebsiteQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        long total = websiteMapper.selectCount(buildPublishedWebsiteQueryWrapper(queryDTO));

        UserWebsitePageVO pageVO = new UserWebsitePageVO();
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));

        if (total == 0) {
            pageVO.setRecords(Collections.emptyList());
            return pageVO;
        }

        int offset = (pageNum - 1) * pageSize;
        LambdaQueryWrapper<Website> listQueryWrapper = buildPublishedWebsiteQueryWrapper(queryDTO)
                .orderByDesc(Website::getIsTop)
                .orderByAsc(Website::getSort)
                .orderByDesc(Website::getUpdateTime)
                .last("limit " + offset + "," + pageSize);

        List<Website> websiteList = websiteMapper.selectList(listQueryWrapper);
        Map<Long, String> categoryNameMap = buildCategoryNameMap(websiteList);

        List<UserWebsiteListItemVO> itemVOS = websiteList.stream()
                .map(website -> {
                    UserWebsiteListItemVO itemVO = BeanUtil.copyProperties(website, UserWebsiteListItemVO.class);
                    itemVO.setCategoryName(categoryNameMap.getOrDefault(website.getCategoryId(), ""));
                    itemVO.setTags(parseTags(website.getTags()));
                    return itemVO;
                })
                .toList();

        pageVO.setRecords(itemVOS);
        return pageVO;
    }

    /**
     * 查询网站分类统计
     *
     * @return 分类统计列表
     */
    @Override
    public List<UserWebsiteCategoryVO> queryCategoryStats() {
        List<WebsiteCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getDeleted, NOT_DELETED)
                .eq(WebsiteCategory::getStatus, CATEGORY_ENABLED_STATUS)
                .orderByAsc(WebsiteCategory::getSort)
                .orderByAsc(WebsiteCategory::getId));

        if (CollUtil.isEmpty(categories)) {
            return Collections.emptyList();
        }

        Map<Long, Long> countMap = countPublishedWebsiteByCategory();

        return categories.stream()
                .map(category -> {
                    UserWebsiteCategoryVO categoryVO = BeanUtil.copyProperties(category, UserWebsiteCategoryVO.class);
                    categoryVO.setCount(countMap.getOrDefault(category.getId(), 0L));
                    return categoryVO;
                })
                .toList();
    }

    /**
     * 查询网站详情
     *
     * @param websiteId 网站ID
     * @return 网站详情
     */
    @Override
    public UserWebsiteDetailVO queryWebsiteDetail(Long websiteId) {
        Website website = websiteMapper.selectOne(new LambdaQueryWrapper<Website>()
                .eq(Website::getId, websiteId)
                .eq(Website::getDeleted, NOT_DELETED)
                .eq(Website::getStatus, ONLINE_STATUS)
                .eq(Website::getAuditStatus, AUDIT_APPROVED_STATUS)
                .last("limit 1"));

        if (website == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "网站不存在或已下架");
        }

        UserWebsiteDetailVO detailVO = BeanUtil.copyProperties(website, UserWebsiteDetailVO.class);
        detailVO.setCategoryName(resolveCategoryName(website.getCategoryId()));
        detailVO.setTags(parseTags(website.getTags()));
        return detailVO;
    }

    /**
     * 构建用户端网站查询条件
     *
     * @param queryDTO 查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<Website> buildPublishedWebsiteQueryWrapper(UserWebsiteQueryDTO queryDTO) {
        LambdaQueryWrapper<Website> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Website::getDeleted, NOT_DELETED)
                .eq(Website::getStatus, ONLINE_STATUS)
                .eq(Website::getAuditStatus, AUDIT_APPROVED_STATUS);

        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(Website::getCategoryId, queryDTO.getCategoryId());
        }

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String normalizedKeyword = queryDTO.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(Website::getName, normalizedKeyword)
                    .or()
                    .like(Website::getUrl, normalizedKeyword)
                    .or()
                    .like(Website::getSummary, normalizedKeyword)
                    .or()
                    .like(Website::getDescription, normalizedKeyword));
        }

        return queryWrapper;
    }

    /**
     * 构建分类名称映射
     *
     * @param websiteList 网站列表
     * @return 分类名称映射
     */
    private Map<Long, String> buildCategoryNameMap(List<Website> websiteList) {
        if (CollUtil.isEmpty(websiteList)) {
            return Collections.emptyMap();
        }

        Set<Long> categoryIds = websiteList.stream()
                .map(Website::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (CollUtil.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }

        List<WebsiteCategory> categories = categoryMapper.selectBatchIds(categoryIds);
        if (CollUtil.isEmpty(categories)) {
            return Collections.emptyMap();
        }

        return categories.stream().collect(Collectors.toMap(
                WebsiteCategory::getId,
                WebsiteCategory::getName,
                (left, right) -> left
        ));
    }

    /**
     * 查询分类名称
     *
     * @param categoryId 分类ID
     * @return 分类名称
     */
    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "";
        }

        WebsiteCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getId, categoryId)
                .eq(WebsiteCategory::getDeleted, NOT_DELETED)
                .last("limit 1"));

        if (category == null || !StringUtils.hasText(category.getName())) {
            return "";
        }

        return category.getName();
    }

    /**
     * 按分类统计已发布网站数量
     *
     * @return 分类数量映射
     */
    private Map<Long, Long> countPublishedWebsiteByCategory() {
        QueryWrapper<Website> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("category_id", "COUNT(1) AS total")
                .eq("deleted", NOT_DELETED)
                .eq("status", ONLINE_STATUS)
                .eq("audit_status", AUDIT_APPROVED_STATUS)
                .isNotNull("category_id")
                .groupBy("category_id");

        List<Map<String, Object>> countList = websiteMapper.selectMaps(queryWrapper);
        if (CollUtil.isEmpty(countList)) {
            return Collections.emptyMap();
        }

        Map<Long, Long> countMap = new HashMap<>(countList.size());
        for (Map<String, Object> row : countList) {
            Long categoryId = toLong(row.get("category_id"));
            Long total = toLong(row.get("total"));
            if (categoryId != null) {
                countMap.put(categoryId, total == null ? 0L : total);
            }
        }

        return countMap;
    }

    /**
     * 解析标签列表
     *
     * @param tags 标签原始文本
     * @return 标签列表
     */
    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Collections.emptyList();
        }

        return Arrays.stream(tags.replace('，', ',').split(","))
                .map(tag -> tag.replaceAll("\\s+", ""))
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    /**
     * 计算总页数
     *
     * @param total 总条数
     * @param pageSize 每页数量
     * @return 总页数
     */
    private Long calcTotalPages(long total, int pageSize) {
        return (total + pageSize - 1L) / pageSize;
    }

    /**
     * 转换数字对象为 Long
     *
     * @param value 原始对象
     * @return Long 值
     */
    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        String valueText = String.valueOf(value);
        if (!StringUtils.hasText(valueText)) {
            return null;
        }

        try {
            return Long.parseLong(valueText);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
