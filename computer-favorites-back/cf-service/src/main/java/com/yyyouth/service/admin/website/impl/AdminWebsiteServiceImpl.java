package com.yyyouth.service.admin.website.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.utils.SensitiveWordUtils;
import com.yyyouth.model.dto.admin.AdminWebsiteCreateDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteQueryDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.admin.AdminWebsiteCategoryVO;
import com.yyyouth.model.vo.admin.AdminWebsiteListItemVO;
import com.yyyouth.model.vo.admin.AdminWebsiteLogoUploadVO;
import com.yyyouth.model.vo.admin.AdminWebsitePageVO;
import com.yyyouth.model.vo.admin.AdminWebsiteStatsVO;
import com.yyyouth.model.vo.file.MinioUploadVO;
import com.yyyouth.service.admin.website.AdminWebsiteService;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    private static final int AUDIT_APPROVED_STATUS = 1;

    private static final int AUDIT_REJECTED_STATUS = 2;

    private static final int CATEGORY_ENABLED_STATUS = 1;

    private static final int ADMIN_SOURCE = 0;

    private static final int DEFAULT_SORT = 0;

    private static final int MAX_TAG_LENGTH = 500;

    private static final long MAX_LOGO_FILE_SIZE = 1024 * 1024;

    private static final String LOGO_PATH_MODULE = "admin";

    private static final String LOGO_PATH_BUSINESS = "website";

    private static final String LOGO_PATH_PURPOSE = "logo";

    private static final String LOGO_OBJECT_KEY_PREFIX = LOGO_PATH_MODULE + "/" + LOGO_PATH_BUSINESS + "/" + LOGO_PATH_PURPOSE + "/";

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 12;

    private final WebsiteMapper websiteMapper;

    private final CategoryMapper categoryMapper;

    private final MinioFileService minioFileService;

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
     * 上传网站 Logo
     *
     * @param file Logo 文件
     * @return 上传结果
     */
    @Override
    public AdminWebsiteLogoUploadVO uploadWebsiteLogo(MultipartFile file) {
        AdminWebsiteLogoUploadVO logoUploadVO = new AdminWebsiteLogoUploadVO();
        MinioUploadVO uploadVO = minioFileService.uploadImageByMonth(
                file,
                LOGO_PATH_MODULE,
                LOGO_PATH_BUSINESS,
                LOGO_PATH_PURPOSE,
                MAX_LOGO_FILE_SIZE
        );
        logoUploadVO.setObjectKey(uploadVO.getObjectKey());
        logoUploadVO.setLogoUrl(uploadVO.getFileUrl());
        return logoUploadVO;
    }

    /**
     * 删除网站 Logo
     *
     * @param objectKey 对象键
     */
    @Override
    public void deleteWebsiteLogo(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        String normalizedObjectKey = objectKey.trim();
        if (!normalizedObjectKey.startsWith(LOGO_OBJECT_KEY_PREFIX)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Logo对象键不合法");
        }
        minioFileService.deleteByObjectKey(normalizedObjectKey);
    }

    /**
     * 创建网站
     *
     * @param createDTO 创建参数
     * @return 网站ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWebsite(AdminWebsiteCreateDTO createDTO) {
        Long adminId = StpAdminUtil.getLoginIdAsLong();
        if (adminId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }

        validateCategoryForCreate(createDTO.getCategoryId());
        validateSensitiveFields(createDTO);
        validateIconValue(createDTO.getIcon());

        Website website = buildWebsiteEntity(createDTO, adminId);
        int insertedRows = websiteMapper.insert(website);
        if (insertedRows != 1 || website.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "添加网站失败，请稍后重试");
        }
        return website.getId();
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

    /**
     * 校验分类是否可用
     *
     * @param categoryId 分类ID
     */
    private void validateCategoryForCreate(Long categoryId) {
        WebsiteCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getId, categoryId)
                .eq(WebsiteCategory::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (category == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类不存在");
        }
        if (!Objects.equals(category.getStatus(), CATEGORY_ENABLED_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类已禁用，无法添加网站");
        }
    }

    /**
     * 校验新增参数中的敏感词
     *
     * @param createDTO 新增参数
     */
    private void validateSensitiveFields(AdminWebsiteCreateDTO createDTO) {
        checkSensitiveField("网站名称", normalizeRequiredText(createDTO.getName()));
        checkSensitiveField("一句话简介", normalizeOptionalText(createDTO.getSummary()));
        checkSensitiveField("详细描述", normalizeOptionalText(createDTO.getDescription()));
        checkSensitiveField("标签", normalizeTags(createDTO.getTags()));
    }

    /**
     * 校验 Logo 地址
     *
     * @param icon Logo 地址
     */
    private void validateIconValue(String icon) {
        if (!StringUtils.hasText(icon)) {
            return;
        }
        String normalizedIcon = icon.trim();
        if (!normalizedIcon.contains("/" + LOGO_OBJECT_KEY_PREFIX)
                && !normalizedIcon.startsWith(LOGO_OBJECT_KEY_PREFIX)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Logo地址不合法，请重新上传Logo");
        }
    }

    /**
     * 检测单个字段敏感词
     *
     * @param fieldName 字段名
     * @param fieldValue 字段值
     */
    private void checkSensitiveField(String fieldName, String fieldValue) {
        if (!StringUtils.hasText(fieldValue)) {
            return;
        }
        if (!SensitiveWordUtils.containsForUserContent(fieldValue)) {
            return;
        }

        String sensitiveWord = SensitiveWordUtils.findFirst(fieldValue);
        String message = StringUtils.hasText(sensitiveWord)
                ? fieldName + "包含敏感词：" + sensitiveWord
                : fieldName + "包含敏感词";
        throw new BusinessException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 构建新增网站实体
     *
     * @param createDTO 新增参数
     * @param adminId 管理员ID
     * @return 网站实体
     */
    private Website buildWebsiteEntity(AdminWebsiteCreateDTO createDTO, Long adminId) {
        Website website = BeanUtil.copyProperties(createDTO, Website.class);
        LocalDateTime now = LocalDateTime.now();

        website.setName(normalizeRequiredText(createDTO.getName()));
        website.setUrl(normalizeUrl(createDTO.getUrl()));
        website.setIcon(normalizeOptionalText(createDTO.getIcon()));
        website.setSummary(normalizeOptionalText(createDTO.getSummary()));
        website.setDescription(normalizeOptionalText(createDTO.getDescription()));
        website.setTags(normalizeTags(createDTO.getTags()));
        website.setSort(createDTO.getSort() == null ? DEFAULT_SORT : createDTO.getSort());
        website.setIsTop(Boolean.TRUE.equals(createDTO.getIsTop()) ? 1 : 0);
        website.setIsRecommend(Boolean.TRUE.equals(createDTO.getIsRecommend()) ? 1 : 0);

        website.setStatus(ONLINE_STATUS);
        website.setDeleted(NOT_DELETED);
        website.setSource(ADMIN_SOURCE);
        website.setAuditStatus(AUDIT_APPROVED_STATUS);
        website.setAuditRemark("管理员录入自动通过");
        website.setSubmitterId(adminId);
        website.setAuditAdminId(toIntExactAdminId(adminId));
        website.setCreateTime(now);
        website.setUpdateTime(now);
        return website;
    }

    /**
     * 标准化必填文本
     *
     * @param text 原始文本
     * @return 标准化结果
     */
    private String normalizeRequiredText(String text) {
        return text == null ? "" : text.trim();
    }

    /**
     * 标准化可选文本
     *
     * @param text 原始文本
     * @return 标准化结果
     */
    private String normalizeOptionalText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.trim();
    }

    /**
     * 标准化 URL
     *
     * @param url 原始URL
     * @return 标准化结果
     */
    private String normalizeUrl(String url) {
        return normalizeRequiredText(url);
    }

    /**
     * 标准化标签
     *
     * @param tags 原始标签
     * @return 标准化后的标签
     */
    private String normalizeTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return "";
        }

        String normalizedTags = Arrays.stream(tags.replace('，', ',').split(","))
            .map(tag -> tag.replaceAll("\\s+", ""))
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining(","));

        if (normalizedTags.length() > MAX_TAG_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标签总长度不能超过500");
        }
        return normalizedTags;
    }

    /**
     * 转换管理员ID
     *
     * @param adminId 管理员ID
     * @return int类型管理员ID
     */
    private Integer toIntExactAdminId(Long adminId) {
        try {
            return Math.toIntExact(adminId);
        } catch (ArithmeticException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "管理员ID不合法");
        }
    }
}
