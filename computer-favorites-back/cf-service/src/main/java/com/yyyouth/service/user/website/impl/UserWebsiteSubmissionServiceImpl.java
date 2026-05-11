package com.yyyouth.service.user.website.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.utils.SensitiveWordUtils;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionCreateDTO;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionQueryDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.file.MinioUploadVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionDetailVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionIconUploadVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionListItemVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionPageVO;
import com.yyyouth.model.vo.user.UserWebsiteTagItemVO;
import com.yyyouth.model.vo.userstats.ProfilePublicVO;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.userstats.ProfilePublicService;
import com.yyyouth.service.user.website.UserWebsiteSubmissionService;
import com.yyyouth.service.user.website.support.UserWebsiteTagSupport;
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
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿网站服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserWebsiteSubmissionServiceImpl implements UserWebsiteSubmissionService {

    private static final int NOT_DELETED = 0;

    private static final int OFFLINE_STATUS = 0;

    private static final int ONLINE_STATUS = 1;

    private static final int AUDIT_PENDING_STATUS = 0;

    private static final int AUDIT_APPROVED_STATUS = 1;

    private static final int AUDIT_REJECTED_STATUS = 2;

    private static final int USER_SOURCE = 1;

    private static final int CATEGORY_ENABLED_STATUS = 1;

    private static final int DEFAULT_SORT = 0;

    private static final int MAX_TAG_LENGTH = 500;

    private static final long MAX_ICON_FILE_SIZE = 1024 * 1024;

    private static final String ICON_PATH_MODULE = "user";

    private static final String ICON_PATH_BUSINESS = "website";

    private static final String ICON_PATH_PURPOSE = "icon";

    private static final String ICON_OBJECT_KEY_PREFIX = ICON_PATH_MODULE + "/" + ICON_PATH_BUSINESS + "/" + ICON_PATH_PURPOSE + "/";

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 12;

    private static final int DEFAULT_PUBLIC_PAGE_SIZE = 5;

    private static final int MAX_PUBLIC_PAGE_SIZE = 50;

    private static final int DEFAULT_AUDIT_ADMIN_ID = 0;

    private static final int BOOLEAN_NO = 0;

    private final WebsiteMapper websiteMapper;

    private final CategoryMapper categoryMapper;

    private final MinioFileService minioFileService;

    private final UserWebsiteTagSupport userWebsiteTagSupport;

    private final ProfilePublicService profilePublicService;

    /**
     * 上传投稿网站图标
     *
     * @param file 图标文件
     * @return 上传结果
     */
    @Override
    public UserWebsiteSubmissionIconUploadVO uploadSubmissionIcon(MultipartFile file) {
        getCurrentUserId();
        MinioUploadVO uploadVO = minioFileService.uploadImageByMonth(
                file,
                ICON_PATH_MODULE,
                ICON_PATH_BUSINESS,
                ICON_PATH_PURPOSE,
                MAX_ICON_FILE_SIZE
        );

        UserWebsiteSubmissionIconUploadVO iconUploadVO = new UserWebsiteSubmissionIconUploadVO();
        iconUploadVO.setObjectKey(uploadVO.getObjectKey());
        iconUploadVO.setIconUrl(uploadVO.getFileUrl());
        return iconUploadVO;
    }

    /**
     * 删除投稿网站图标
     *
     * @param objectKey 对象键
     */
    @Override
    public void deleteSubmissionIcon(String objectKey) {
        getCurrentUserId();
        if (!StringUtils.hasText(objectKey)) {
            return;
        }

        String normalizedObjectKey = objectKey.trim();
        if (!normalizedObjectKey.startsWith(ICON_OBJECT_KEY_PREFIX)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图标对象键不合法");
        }
        minioFileService.deleteByObjectKey(normalizedObjectKey);
    }

    /**
     * 提交网站投稿
     *
     * @param createDTO 投稿参数
     * @return 网站ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitWebsite(UserWebsiteSubmissionCreateDTO createDTO) {
        Long userId = getCurrentUserId();
        validateCategoryForSubmission(createDTO.getCategoryId());
        validateSensitiveFields(createDTO);

        LocalDateTime now = LocalDateTime.now();
        Website website = new Website();
        website.setName(normalizeRequiredText(createDTO.getName()));
        website.setUrl(normalizeUrl(createDTO.getUrl()));
        website.setIcon(normalizeOptionalText(createDTO.getIcon()));
        website.setGithubUrl(normalizeOptionalText(createDTO.getGithubUrl()));
        website.setSummary(normalizeRequiredText(createDTO.getSummary()));
        website.setDescription(normalizeOptionalText(createDTO.getDescription()));
        website.setCategoryId(createDTO.getCategoryId());
        website.setTags(normalizeTags(createDTO.getTags()));
        website.setSort(DEFAULT_SORT);
        website.setIsTop(BOOLEAN_NO);
        website.setIsRecommend(BOOLEAN_NO);
        website.setStatus(OFFLINE_STATUS);
        website.setSource(USER_SOURCE);
        website.setSubmitterId(userId);
        website.setAuditStatus(AUDIT_PENDING_STATUS);
        website.setAuditRemark("");
        website.setAuditAdminId(DEFAULT_AUDIT_ADMIN_ID);
        website.setDeleted(NOT_DELETED);
        website.setIsOfficial(BOOLEAN_NO);
        website.setCreateTime(now);
        website.setUpdateTime(now);

        int insertedRows = websiteMapper.insert(website);
        if (insertedRows != 1 || website.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "网站投稿失败，请稍后重试");
        }
        return website.getId();
    }

    /**
     * 查询我的投稿分页
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public UserWebsiteSubmissionPageVO queryMySubmissionPage(UserWebsiteSubmissionQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        LambdaQueryWrapper<Website> queryWrapper = buildMySubmissionQueryWrapper(userId, queryDTO);
        long total = websiteMapper.selectCount(queryWrapper);

        UserWebsiteSubmissionPageVO pageVO = new UserWebsiteSubmissionPageVO();
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));

        if (total == 0L) {
            pageVO.setRecords(Collections.emptyList());
            return pageVO;
        }

        int offset = (pageNum - 1) * pageSize;
        List<Website> websiteList = websiteMapper.selectList(buildMySubmissionQueryWrapper(userId, queryDTO)
                .orderByDesc(Website::getUpdateTime)
                .last("limit " + offset + "," + pageSize));

        pageVO.setRecords(buildSubmissionListItems(websiteList));
        return pageVO;
    }

    @Override
    public UserWebsiteSubmissionPageVO queryPublicApprovedSubmissionPage(String username, Long currentUserId, Integer limit) {
        ProfilePublicVO publicVO = profilePublicService.getPublicProfile(username, currentUserId);
        if (publicVO == null || publicVO.getUser() == null || publicVO.getUser().getId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "公开主页用户不存在");
        }

        Long targetUserId = publicVO.getUser().getId();
        int pageSize = normalizePublicPageSize(limit);
        LambdaQueryWrapper<Website> queryWrapper = buildPublicApprovedSubmissionQueryWrapper(targetUserId);
        long total = websiteMapper.selectCount(queryWrapper);

        UserWebsiteSubmissionPageVO pageVO = new UserWebsiteSubmissionPageVO();
        pageVO.setTotal(total);
        pageVO.setPageNum(DEFAULT_PAGE_NUM);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));

        if (total == 0L) {
            pageVO.setRecords(Collections.emptyList());
            return pageVO;
        }

        List<Website> websiteList = websiteMapper.selectList(buildPublicApprovedSubmissionQueryWrapper(targetUserId)
                .orderByDesc(Website::getUpdateTime)
                .last("limit " + pageSize));

        pageVO.setRecords(buildSubmissionListItems(websiteList));
        return pageVO;
    }

    /**
     * 查询我的投稿详情
     *
     * @param websiteId 网站ID
     * @return 投稿详情
     */
    @Override
    public UserWebsiteSubmissionDetailVO queryMySubmissionDetail(Long websiteId) {
        Long userId = getCurrentUserId();
        Website website = queryOwnedSubmissionById(websiteId, userId);

        UserWebsiteSubmissionDetailVO detailVO = BeanUtil.copyProperties(
            website,
            UserWebsiteSubmissionDetailVO.class,
            "tags"
        );
        detailVO.setCategoryName(resolveCategoryName(website.getCategoryId()));
        Map<Long, UserWebsiteTagItemVO> tagItemMap = userWebsiteTagSupport.buildTagItemMap(
            userWebsiteTagSupport.parseTagIds(website.getTags())
        );
        detailVO.setTags(userWebsiteTagSupport.buildWebsiteTagItems(website.getTags(), tagItemMap));
        return detailVO;
    }

    /**
     * 编辑我的投稿
     *
     * @param websiteId 网站ID
     * @param editDTO 编辑参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editMySubmission(Long websiteId, UserWebsiteSubmissionCreateDTO editDTO) {
        Long userId = getCurrentUserId();
        Website submission = queryOwnedSubmissionById(websiteId, userId);
        if (!Objects.equals(submission.getAuditStatus(), AUDIT_PENDING_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅待审核的投稿可编辑");
        }

        validateCategoryForSubmission(editDTO.getCategoryId());
        validateSensitiveFields(editDTO);

        Website updateEntity = new Website();
        updateEntity.setId(websiteId);
        updateEntity.setName(normalizeRequiredText(editDTO.getName()));
        updateEntity.setUrl(normalizeUrl(editDTO.getUrl()));
        updateEntity.setIcon(normalizeOptionalText(editDTO.getIcon()));
        updateEntity.setGithubUrl(normalizeOptionalText(editDTO.getGithubUrl()));
        updateEntity.setSummary(normalizeRequiredText(editDTO.getSummary()));
        updateEntity.setDescription(normalizeOptionalText(editDTO.getDescription()));
        updateEntity.setCategoryId(editDTO.getCategoryId());
        updateEntity.setTags(normalizeTags(editDTO.getTags()));
        updateEntity.setUpdateTime(LocalDateTime.now());

        int updatedRows = websiteMapper.updateById(updateEntity);
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "投稿编辑失败，请稍后重试");
        }
    }

    /**
     * 取消我的投稿
     *
     * @param websiteId 网站ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMySubmission(Long websiteId) {
        Long userId = getCurrentUserId();
        Website submission = queryOwnedSubmissionById(websiteId, userId);
        if (!Objects.equals(submission.getAuditStatus(), AUDIT_PENDING_STATUS)
                && !Objects.equals(submission.getAuditStatus(), AUDIT_REJECTED_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅待审核和已拒绝的投稿可取消");
        }

        Website updateEntity = new Website();
        updateEntity.setId(websiteId);
        updateEntity.setDeleted(1);
        updateEntity.setUpdateTime(LocalDateTime.now());

        int updatedRows = websiteMapper.updateById(updateEntity);
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "取消投稿失败，请稍后重试");
        }
    }

    /**
     * 重新提交我的投稿
     *
     * @param websiteId 网站ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmitMySubmission(Long websiteId) {
        Long userId = getCurrentUserId();
        Website submission = queryOwnedSubmissionById(websiteId, userId);
        if (!Objects.equals(submission.getAuditStatus(), AUDIT_REJECTED_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅已拒绝的投稿可重新提交");
        }

        Website updateEntity = new Website();
        updateEntity.setId(websiteId);
        updateEntity.setAuditStatus(AUDIT_PENDING_STATUS);
        updateEntity.setStatus(OFFLINE_STATUS);
        updateEntity.setAuditRemark("");
        updateEntity.setAuditAdminId(DEFAULT_AUDIT_ADMIN_ID);
        updateEntity.setUpdateTime(LocalDateTime.now());

        int updatedRows = websiteMapper.updateById(updateEntity);
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "重新提交失败，请稍后重试");
        }
    }

    /**
     * 构建我的投稿查询条件
     *
     * @param userId 用户ID
     * @param queryDTO 查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<Website> buildMySubmissionQueryWrapper(Long userId, UserWebsiteSubmissionQueryDTO queryDTO) {
        LambdaQueryWrapper<Website> queryWrapper = new LambdaQueryWrapper<Website>()
                .eq(Website::getDeleted, NOT_DELETED)
                .eq(Website::getSource, USER_SOURCE)
                .eq(Website::getSubmitterId, userId);

        if (queryDTO.getAuditStatus() != null) {
            queryWrapper.eq(Website::getAuditStatus, queryDTO.getAuditStatus());
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
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        if (userId == null || userId <= 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户未登录");
        }
        return userId;
    }

    /**
     * 校验分类是否可用
     *
     * @param categoryId 分类ID
     */
    private void validateCategoryForSubmission(Long categoryId) {
        WebsiteCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getId, categoryId)
                .eq(WebsiteCategory::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (category == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类不存在");
        }
        if (!Objects.equals(category.getStatus(), CATEGORY_ENABLED_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类已禁用，无法保存投稿");
        }
    }

    /**
     * 校验投稿敏感词
     *
     * @param createDTO 投稿参数
     */
    private void validateSensitiveFields(UserWebsiteSubmissionCreateDTO createDTO) {
        checkSensitiveField("网站名称", normalizeRequiredText(createDTO.getName()));
        checkSensitiveField("网站简介", normalizeRequiredText(createDTO.getSummary()));
        checkSensitiveField("详细描述", normalizeOptionalText(createDTO.getDescription()));
        checkSensitiveField("标签", normalizeTags(createDTO.getTags()));
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
     * 查询用户自己的投稿
     *
     * @param websiteId 网站ID
     * @param userId 用户ID
     * @return 投稿实体
     */
    private Website queryOwnedSubmissionById(Long websiteId, Long userId) {
        Website website = websiteMapper.selectOne(new LambdaQueryWrapper<Website>()
                .eq(Website::getId, websiteId)
                .eq(Website::getDeleted, NOT_DELETED)
                .eq(Website::getSource, USER_SOURCE)
                .eq(Website::getSubmitterId, userId)
                .last("limit 1"));
        if (website == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "投稿不存在或无权限操作");
        }
        return website;
    }

    private LambdaQueryWrapper<Website> buildPublicApprovedSubmissionQueryWrapper(Long targetUserId) {
        return new LambdaQueryWrapper<Website>()
                .eq(Website::getDeleted, NOT_DELETED)
                .eq(Website::getStatus, ONLINE_STATUS)
                .eq(Website::getSource, USER_SOURCE)
                .eq(Website::getSubmitterId, targetUserId)
                .eq(Website::getAuditStatus, AUDIT_APPROVED_STATUS);
    }

    private List<UserWebsiteSubmissionListItemVO> buildSubmissionListItems(List<Website> websiteList) {
        if (CollUtil.isEmpty(websiteList)) {
            return Collections.emptyList();
        }

        Map<Long, String> categoryNameMap = buildCategoryNameMap(websiteList);
        Set<Long> tagIds = websiteList.stream()
                .flatMap(website -> userWebsiteTagSupport.parseTagIds(website.getTags()).stream())
                .collect(Collectors.toSet());
        Map<Long, UserWebsiteTagItemVO> tagItemMap = userWebsiteTagSupport.buildTagItemMap(tagIds);

        return websiteList.stream()
                .map(website -> {
                    UserWebsiteSubmissionListItemVO itemVO = BeanUtil.copyProperties(
                            website,
                            UserWebsiteSubmissionListItemVO.class,
                            "tags"
                    );
                    itemVO.setCategoryName(categoryNameMap.getOrDefault(website.getCategoryId(), ""));
                    itemVO.setTags(userWebsiteTagSupport.buildWebsiteTagItems(website.getTags(), tagItemMap));
                    return itemVO;
                })
                .toList();
    }

    private int normalizePublicPageSize(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_PUBLIC_PAGE_SIZE;
        }
        return Math.min(limit, MAX_PUBLIC_PAGE_SIZE);
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

        List<WebsiteCategory> categoryList = categoryMapper.selectBatchIds(categoryIds);
        if (CollUtil.isEmpty(categoryList)) {
            return Collections.emptyMap();
        }

        Map<Long, String> categoryNameMap = new HashMap<>(categoryList.size());
        categoryList.forEach(category -> categoryNameMap.put(category.getId(), category.getName()));
        return categoryNameMap;
    }

    /**
     * 解析分类名称
     *
     * @param categoryId 分类ID
     * @return 分类名称
     */
    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "";
        }
        WebsiteCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !StringUtils.hasText(category.getName())) {
            return "";
        }
        return category.getName();
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
     * 计算总页数
     *
     * @param total 总条数
     * @param pageSize 每页数量
     * @return 总页数
     */
    private long calcTotalPages(long total, int pageSize) {
        return (total + pageSize - 1L) / pageSize;
    }
}
