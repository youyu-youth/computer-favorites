package com.yyyouth.service.user.website.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserWebsiteQueryDTO;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.UserCollect;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.pojo.website.WebsiteLike;
import com.yyyouth.model.pojo.website.WebsiteScore;
import com.yyyouth.model.vo.user.UserWebsiteCategoryVO;
import com.yyyouth.model.vo.user.UserWebsiteDetailVO;
import com.yyyouth.model.vo.user.UserWebsiteListItemVO;
import com.yyyouth.model.vo.user.UserWebsitePageVO;
import com.yyyouth.model.vo.user.UserWebsiteTagItemVO;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.mapper.user.UserCollectMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteLikeMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.mapper.website.WebsiteScoreMapper;
import com.yyyouth.service.user.website.UserWebsiteService;
import com.yyyouth.service.user.website.support.UserWebsiteTagSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
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

    private static final int ADMIN_SOURCE = 0;

    private static final int USER_SOURCE = 1;

    private static final String DEFAULT_ADMIN_PROVIDER_NAME = "管理员";

    private static final int CATEGORY_ENABLED_STATUS = 1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 12;

    private final WebsiteMapper websiteMapper;

    private final CategoryMapper categoryMapper;

    private final UserAccountMapper userAccountMapper;

    private final AdminAccountMapper adminAccountMapper;

    private final UserCollectMapper userCollectMapper;

    private final WebsiteLikeMapper websiteLikeMapper;

    private final WebsiteScoreMapper websiteScoreMapper;

    private final UserWebsiteTagSupport userWebsiteTagSupport;

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
        Set<Long> tagIds = websiteList.stream()
                .flatMap(website -> userWebsiteTagSupport.parseTagIds(website.getTags()).stream())
                .collect(Collectors.toSet());
        Map<Long, UserWebsiteTagItemVO> tagItemMap = userWebsiteTagSupport.buildTagItemMap(tagIds);

        List<UserWebsiteListItemVO> itemVOS = websiteList.stream()
                .map(website -> {
                    UserWebsiteListItemVO itemVO = BeanUtil.copyProperties(
                            website,
                            UserWebsiteListItemVO.class,
                            "tags"
                    );
                    itemVO.setCategoryName(categoryNameMap.getOrDefault(website.getCategoryId(), ""));
                    itemVO.setTags(userWebsiteTagSupport.buildWebsiteTagItems(website.getTags(), tagItemMap));
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

        UserWebsiteDetailVO detailVO = BeanUtil.copyProperties(
            website,
            UserWebsiteDetailVO.class,
                "tags"
        );
        detailVO.setCategoryName(resolveCategoryName(website.getCategoryId()));
        Map<Long, UserWebsiteTagItemVO> tagItemMap = userWebsiteTagSupport.buildTagItemMap(
                userWebsiteTagSupport.parseTagIds(website.getTags())
        );
        detailVO.setTags(userWebsiteTagSupport.buildWebsiteTagItems(website.getTags(), tagItemMap));
        detailVO.setProviderName(resolveProviderName(website));
        detailVO.setIsCollected(resolveIsCollected(websiteId));
        detailVO.setIsLiked(resolveIsLiked(websiteId));
        detailVO.setUserScore(resolveUserScore(websiteId));

        /**
         * 从 t_website_score 实时统计平均分和评分人数
         * 若 t_website 表中存储值与实时统计不一致，触发重算修正数据库
         */
        Map<String, Object> scoreStats = websiteScoreMapper.selectScoreStats(websiteId);
        if (scoreStats != null) {
            BigDecimal realScore = scoreStats.get("avgScore") instanceof BigDecimal
                    ? (BigDecimal) scoreStats.get("avgScore") : BigDecimal.ZERO;
            Long realCount = scoreStats.get("scoreCount") instanceof Number
                    ? ((Number) scoreStats.get("scoreCount")).longValue() : 0L;
            detailVO.setScore(realScore);
            detailVO.setScoreCount(realCount.intValue());

            boolean needRecalculate = false;
            BigDecimal currentScore = website.getScore();
            Integer currentCount = website.getScoreCount();
            if (currentScore == null || currentScore.compareTo(realScore) != 0) {
                needRecalculate = true;
            }
            if (currentCount == null || currentCount != realCount.intValue()) {
                needRecalculate = true;
            }
            if (needRecalculate) {
                websiteMapper.recalculateScore(websiteId);
            }
        }

        return detailVO;
    }

    /**
     * 增加网站点击量
     *
     * @param websiteId 网站ID
     */
    @Override
    public void incrementClickCount(Long websiteId) {
        websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                .eq(Website::getId, websiteId)
                .eq(Website::getDeleted, NOT_DELETED)
                .eq(Website::getStatus, ONLINE_STATUS)
                .eq(Website::getAuditStatus, AUDIT_APPROVED_STATUS)
                .setSql("click_count = click_count + 1"));
    }

    /**
     * 判断当前用户是否已收藏该网站
     *
     * @param websiteId 网站ID
     * @return 是否已收藏
     */
    private Boolean resolveIsCollected(Long websiteId) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Long count = userCollectMapper.selectCount(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getWebsiteId, websiteId));
        return count != null && count > 0;
    }

    /**
     * 判断当前用户是否已点赞该网站
     *
     * @param websiteId 网站ID
     * @return 是否已点赞
     */
    private Boolean resolveIsLiked(Long websiteId) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Long count = websiteLikeMapper.selectCount(new LambdaQueryWrapper<WebsiteLike>()
                .eq(WebsiteLike::getUserId, userId)
                .eq(WebsiteLike::getWebsiteId, websiteId));
        return count != null && count > 0;
    }

    /**
     * 查询当前用户对该网站的评分
     *
     * @param websiteId 网站ID
     * @return 评分值（1-5），未评过时返回null
     */
    private Integer resolveUserScore(Long websiteId) {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        WebsiteScore websiteScore = websiteScoreMapper.selectOne(new LambdaQueryWrapper<WebsiteScore>()
                .eq(WebsiteScore::getUserId, userId)
                .eq(WebsiteScore::getWebsiteId, websiteId)
                .last("limit 1"));
        return websiteScore != null ? websiteScore.getScore() : null;
    }

    /**
     * 解析网站提供者名称
     *
     * @param website 网站实体
     * @return 提供者名称
     */
    private String resolveProviderName(Website website) {
        Long submitterId = website.getSubmitterId();
        Integer source = website.getSource();
        if (Objects.equals(source, ADMIN_SOURCE) && (submitterId == null || submitterId <= 0)) {
            return DEFAULT_ADMIN_PROVIDER_NAME;
        }
        if (submitterId == null || submitterId <= 0) {
            return "";
        }

        if (Objects.equals(source, USER_SOURCE)) {
            return resolveUserProviderName(submitterId);
        }
        if (Objects.equals(source, ADMIN_SOURCE)) {
            String adminProviderName = resolveAdminProviderName(submitterId);
            if (StringUtils.hasText(adminProviderName)) {
                return adminProviderName;
            }
            return DEFAULT_ADMIN_PROVIDER_NAME;
        }

        String userProviderName = resolveUserProviderName(submitterId);
        if (StringUtils.hasText(userProviderName)) {
            return userProviderName;
        }
        return resolveAdminProviderName(submitterId);
    }

    /**
     * 解析用户来源的提供者名称
     *
     * @param submitterId 提交用户ID
     * @return 提供者名称
     */
    private String resolveUserProviderName(Long submitterId) {
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, submitterId)
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (userAccount == null) {
            return "";
        }
        return resolveAccountDisplayName(userAccount.getNickname(), userAccount.getUsername());
    }

    /**
     * 解析管理员来源的提供者名称
     *
     * @param submitterId 提交用户ID
     * @return 提供者名称
     */
    private String resolveAdminProviderName(Long submitterId) {
        AdminAccount adminAccount = adminAccountMapper.selectOne(new LambdaQueryWrapper<AdminAccount>()
                .eq(AdminAccount::getId, submitterId)
                .eq(AdminAccount::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (adminAccount == null) {
            return "";
        }
        return resolveAccountDisplayName(adminAccount.getNickname(), adminAccount.getUsername());
    }

    /**
     * 解析账号显示名称
     *
     * @param nickname 昵称
     * @param username 用户名
     * @return 展示名称
     */
    private String resolveAccountDisplayName(String nickname, String username) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        if (StringUtils.hasText(username)) {
            return username.trim();
        }
        return "";
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

        if (CollUtil.isNotEmpty(queryDTO.getTagIds())) {
            List<Long> normalizedTagIds = normalizeQueryTagIds(queryDTO.getTagIds());
            if (CollUtil.isEmpty(normalizedTagIds)) {
                queryWrapper.apply("1 = 0");
                return queryWrapper;
            }
            appendTagFilterCondition(queryWrapper, normalizedTagIds);
        }

        return queryWrapper;
    }

    /**
     * 追加标签筛选条件（OR 命中）
     *
     * @param queryWrapper 查询条件
     * @param tagIds 标签ID列表
     */
    private void appendTagFilterCondition(LambdaQueryWrapper<Website> queryWrapper, List<Long> tagIds) {
        queryWrapper.and(wrapper -> {
            boolean firstCondition = true;
            for (Long tagId : tagIds) {
                if (firstCondition) {
                    wrapper.apply("FIND_IN_SET({0}, REPLACE(REPLACE(IFNULL(tags, ''), '，', ','), ' ', '')) > 0", tagId);
                    firstCondition = false;
                    continue;
                }
                wrapper.or().apply("FIND_IN_SET({0}, REPLACE(REPLACE(IFNULL(tags, ''), '，', ','), ' ', '')) > 0", tagId);
            }
        });
    }

    /**
     * 归一化查询标签ID列表
     *
     * @param tagIds 原始标签ID列表
     * @return 归一化标签ID列表
     */
    private List<Long> normalizeQueryTagIds(List<Long> tagIds) {
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptyList();
        }
        return tagIds.stream()
                .filter(Objects::nonNull)
                .filter(tagId -> tagId > 0)
                .distinct()
                .toList();
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
