package com.yyyouth.service.admin.website.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminWebsiteQueryDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.admin.AdminWebsiteCategoryVO;
import com.yyyouth.model.vo.admin.AdminWebsiteListItemVO;
import com.yyyouth.model.vo.admin.AdminWebsitePageVO;
import com.yyyouth.model.vo.admin.AdminWebsiteStatsVO;
import com.yyyouth.service.admin.website.AdminWebsiteService;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-01
 *
 * 管理端网站治理服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class AdminWebsiteServiceImpl implements AdminWebsiteService {

    private static final int ALL_DELETED_FLAG = -1;

    private static final int NOT_DELETED = 0;

    private static final int DELETED = 1;

    private static final int ONLINE_STATUS = 1;

    private static final int OFFLINE_STATUS = 0;

    private static final int AUDIT_PENDING_STATUS = 0;

    private static final int AUDIT_REJECTED_STATUS = 2;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 12;

    private final WebsiteMapper websiteMapper;

    private final CategoryMapper categoryMapper;

    /**
     * 查询网站分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @Override
    public AdminWebsitePageVO queryWebsitePage(AdminWebsiteQueryDTO queryDTO) {
        int deletedFlag = normalizeDeletedFlag(queryDTO.getDeleted());
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        long total = websiteMapper.selectCount(buildWebsiteQueryWrapper(queryDTO, deletedFlag));

        AdminWebsitePageVO pageVO = new AdminWebsitePageVO();
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));

        if (total == 0) {
            pageVO.setRecords(Collections.emptyList());
            return pageVO;
        }

        int offset = (pageNum - 1) * pageSize;
        LambdaQueryWrapper<Website> listQueryWrapper = buildWebsiteQueryWrapper(queryDTO, deletedFlag)
                .orderByDesc(Website::getIsTop)
                .orderByAsc(Website::getSort)
                .orderByDesc(Website::getUpdateTime)
                .last("limit " + offset + "," + pageSize);

        List<Website> websiteList = websiteMapper.selectList(listQueryWrapper);
        Map<Long, String> categoryNameMap = buildCategoryNameMap(websiteList);

        List<AdminWebsiteListItemVO> itemVOS = websiteList.stream()
                .map(website -> {
                    AdminWebsiteListItemVO itemVO = BeanUtil.copyProperties(website, AdminWebsiteListItemVO.class);
                    itemVO.setCategoryName(categoryNameMap.get(website.getCategoryId()));
                    return itemVO;
                })
                .toList();
        pageVO.setRecords(itemVOS);
        return pageVO;
    }

    /**
     * 查询网站分类统计
     *
     * @param deleted 删除筛选
     * @return 分类统计
     */
    @Override
    public List<AdminWebsiteCategoryVO> queryCategoryStats(Integer deleted) {
        int deletedFlag = normalizeDeletedFlag(deleted);
        List<WebsiteCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getDeleted, NOT_DELETED)
                .orderByAsc(WebsiteCategory::getSort)
                .orderByAsc(WebsiteCategory::getId));
        if (CollUtil.isEmpty(categories)) {
            return Collections.emptyList();
        }

        Map<Long, Long> countMap = countWebsiteByCategory(deletedFlag);
        return categories.stream()
                .map(category -> {
                    AdminWebsiteCategoryVO categoryVO = BeanUtil.copyProperties(category, AdminWebsiteCategoryVO.class);
                    categoryVO.setCount(countMap.getOrDefault(category.getId(), 0L));
                    return categoryVO;
                })
                .toList();
    }

    /**
     * 查询网站统计信息
     *
     * @param deleted 删除筛选
     * @return 网站统计
     */
    @Override
    public AdminWebsiteStatsVO queryWebsiteStats(Integer deleted) {
        int deletedFlag = normalizeDeletedFlag(deleted);
        AdminWebsiteStatsVO statsVO = new AdminWebsiteStatsVO();
        statsVO.setTotal(countByCondition(deletedFlag, null));
        statsVO.setOnline(countByCondition(deletedFlag, wrapper -> wrapper.eq(Website::getStatus, ONLINE_STATUS)));
        statsVO.setOffline(countByCondition(deletedFlag, wrapper -> wrapper.eq(Website::getStatus, OFFLINE_STATUS)));
        statsVO.setPendingAudit(countByCondition(deletedFlag, wrapper -> wrapper.eq(Website::getAuditStatus, AUDIT_PENDING_STATUS)));
        statsVO.setRejectedAudit(countByCondition(deletedFlag, wrapper -> wrapper.eq(Website::getAuditStatus, AUDIT_REJECTED_STATUS)));
        statsVO.setDeleted(countByCondition(deletedFlag, wrapper -> wrapper.eq(Website::getDeleted, DELETED)));
        statsVO.setLatestUpdateTime(queryLatestUpdateTime(deletedFlag));
        return statsVO;
    }

    /**
     * 构建网站列表查询条件
     *
     * @param queryDTO 查询参数
     * @param deletedFlag 删除筛选
     * @return 查询条件
     */
    private LambdaQueryWrapper<Website> buildWebsiteQueryWrapper(AdminWebsiteQueryDTO queryDTO, int deletedFlag) {
        LambdaQueryWrapper<Website> queryWrapper = new LambdaQueryWrapper<>();

        if (deletedFlag != ALL_DELETED_FLAG) {
            queryWrapper.eq(Website::getDeleted, deletedFlag);
        }

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
     * @return 分类映射
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
     * 按分类统计网站数量
     *
     * @param deletedFlag 删除筛选
     * @return 分类统计映射
     */
    private Map<Long, Long> countWebsiteByCategory(int deletedFlag) {
        QueryWrapper<Website> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("category_id", "COUNT(1) AS total")
                .isNotNull("category_id")
                .groupBy("category_id");

        if (deletedFlag != ALL_DELETED_FLAG) {
            queryWrapper.eq("deleted", deletedFlag);
        }

        List<Map<String, Object>> countList = websiteMapper.selectMaps(queryWrapper);
        if (CollUtil.isEmpty(countList)) {
            return Collections.emptyMap();
        }

        Map<Long, Long> countMap = new HashMap<>(countList.size());
        for (Map<String, Object> item : countList) {
            Long categoryId = toLong(item.get("category_id"));
            Long total = toLong(item.get("total"));
            if (categoryId != null) {
                countMap.put(categoryId, total == null ? 0L : total);
            }
        }
        return countMap;
    }

    /**
     * 统计网站数量
     *
     * @param deletedFlag 删除筛选
     * @param conditionAppender 额外条件
     * @return 数量
     */
    private Long countByCondition(int deletedFlag, Consumer<LambdaQueryWrapper<Website>> conditionAppender) {
        LambdaQueryWrapper<Website> queryWrapper = new LambdaQueryWrapper<>();
        if (deletedFlag != ALL_DELETED_FLAG) {
            queryWrapper.eq(Website::getDeleted, deletedFlag);
        }

        if (conditionAppender != null) {
            conditionAppender.accept(queryWrapper);
        }
        return websiteMapper.selectCount(queryWrapper);
    }

    /**
     * 查询最新更新时间
     *
     * @param deletedFlag 删除筛选
     * @return 最新更新时间
     */
    private LocalDateTime queryLatestUpdateTime(int deletedFlag) {
        LambdaQueryWrapper<Website> queryWrapper = new LambdaQueryWrapper<>();
        if (deletedFlag != ALL_DELETED_FLAG) {
            queryWrapper.eq(Website::getDeleted, deletedFlag);
        }

        queryWrapper.select(Website::getUpdateTime)
                .orderByDesc(Website::getUpdateTime)
                .last("limit 1");

        List<Website> websites = websiteMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(websites)) {
            return null;
        }
        return websites.get(0).getUpdateTime();
    }

    /**
     * 计算总页数
     *
     * @param total 总条数
     * @param pageSize 每页大小
     * @return 总页数
     */
    private Long calcTotalPages(long total, int pageSize) {
        return (total + pageSize - 1L) / pageSize;
    }

    /**
     * 标准化删除筛选参数
     *
     * @param deleted 删除筛选
     * @return 标准化结果
     */
    private int normalizeDeletedFlag(Integer deleted) {
        if (deleted == null) {
            return ALL_DELETED_FLAG;
        }
        if (deleted == ALL_DELETED_FLAG || deleted == NOT_DELETED || deleted == DELETED) {
            return deleted;
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, "删除筛选参数不合法");
    }

    /**
     * 转换数字类型
     *
     * @param value 原始值
     * @return Long值
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
