package com.yyyouth.service.admin.website.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminWebsiteAuditDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteBatchAuditDTO;
import com.yyyouth.common.utils.SensitiveWordUtils;
import com.yyyouth.model.dto.admin.AdminWebsiteBatchStatusUpdateDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteCreateDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteEditDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteQueryDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteStatusUpdateDTO;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.system.SystemMessage;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.admin.AdminWebsiteBatchAuditFailItemVO;
import com.yyyouth.model.vo.admin.AdminWebsiteBatchAuditResultVO;
import com.yyyouth.model.vo.admin.AdminWebsiteCategoryVO;
import com.yyyouth.model.vo.admin.AdminWebsiteDetailVO;
import com.yyyouth.model.vo.admin.AdminWebsiteListItemVO;
import com.yyyouth.model.vo.admin.AdminWebsiteLogoUploadVO;
import com.yyyouth.model.vo.admin.AdminWebsitePageVO;
import com.yyyouth.model.vo.admin.AdminWebsiteStatsVO;
import com.yyyouth.model.vo.file.MinioUploadVO;
import com.yyyouth.service.admin.website.AdminWebsiteService;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.mapper.system.SystemMessageMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.TagMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
@Slf4j
public class AdminWebsiteServiceImpl implements AdminWebsiteService {

    private static final int ALL_DELETED_FLAG = -1;

    private static final int NOT_DELETED = 0;

    private static final int DELETED = 1;

    private static final int ONLINE_STATUS = 1;

    private static final int OFFLINE_STATUS = 0;

    private static final int AUDIT_PENDING_STATUS = 0;

    private static final int AUDIT_APPROVED_STATUS = 1;

    private static final int AUDIT_REJECTED_STATUS = 2;

    private static final int AUDIT_BUCKET_PENDING = 0;

    private static final int AUDIT_BUCKET_AUDITED = 1;

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

    private static final int AUDIT_APPROVE_ACTION = 1;

    private static final int AUDIT_REJECT_ACTION = 2;

    private static final int DEFAULT_AUDIT_MESSAGE_TYPE = 4;

    private static final int UNREAD_MESSAGE = 0;

    private static final String AUDIT_ACTION_SINGLE = "audit";

    private static final String AUDIT_ACTION_BATCH = "batch-audit";

    private static final String AUDIT_APPROVE_REMARK = "审核通过";

    private final WebsiteMapper websiteMapper;

    private final CategoryMapper categoryMapper;

    private final MinioFileService minioFileService;

    private final SystemMessageMapper systemMessageMapper;

    private final TagMapper tagMapper;

    private final UserAccountMapper userAccountMapper;

    private final AdminAccountMapper adminAccountMapper;

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
     * 查询网站详情
     *
     * @param websiteId 网站ID
     * @return 网站详情
     */
    @Override
    public AdminWebsiteDetailVO queryWebsiteDetail(Long websiteId) {
        Website website = websiteMapper.selectById(websiteId);
        if (website == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "网站不存在");
        }

        AdminWebsiteDetailVO detailVO = BeanUtil.copyProperties(website, AdminWebsiteDetailVO.class);
        detailVO.setCategoryName(resolveCategoryName(website.getCategoryId()));
        detailVO.setTagNameList(resolveTagNameList(website.getTags()));
        detailVO.setSubmitterName(resolveSubmitterName(website.getSubmitterId()));
        detailVO.setAuditAdminName(resolveAuditAdminName(website.getAuditAdminId()));
        return detailVO;
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
     * 编辑网站
     *
     * @param websiteId 网站ID
     * @param editDTO 编辑参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editWebsite(Long websiteId, AdminWebsiteEditDTO editDTO) {
        Website website = queryAvailableWebsiteById(websiteId);
        String normalizedIcon = normalizeOptionalText(editDTO.getIcon());
        String originalIcon = normalizeOptionalText(website.getIcon());

        validateCategoryForCreate(editDTO.getCategoryId());
        validateSensitiveFields(editDTO);
        if (!Objects.equals(normalizedIcon, originalIcon)) {
            validateIconValue(editDTO.getIcon());
        }

        Website updateEntity = new Website();
        updateEntity.setId(websiteId);
        updateEntity.setName(normalizeRequiredText(editDTO.getName()));
        updateEntity.setUrl(normalizeUrl(editDTO.getUrl()));
        updateEntity.setIcon(normalizedIcon);
        updateEntity.setSummary(normalizeOptionalText(editDTO.getSummary()));
        updateEntity.setDescription(normalizeOptionalText(editDTO.getDescription()));
        updateEntity.setCategoryId(editDTO.getCategoryId());
        updateEntity.setTags(normalizeTags(editDTO.getTags()));
        updateEntity.setSort(editDTO.getSort() == null ? DEFAULT_SORT : editDTO.getSort());
        updateEntity.setIsTop(Boolean.TRUE.equals(editDTO.getIsTop()) ? 1 : 0);
        updateEntity.setIsRecommend(Boolean.TRUE.equals(editDTO.getIsRecommend()) ? 1 : 0);

        boolean hasAuditFieldChange = editDTO.getIsOfficial() != null || editDTO.getAuditRemark() != null;
        if (Objects.equals(website.getAuditStatus(), AUDIT_PENDING_STATUS)) {
            if (editDTO.getIsOfficial() != null) {
                updateEntity.setIsOfficial(Boolean.TRUE.equals(editDTO.getIsOfficial()) ? 1 : 0);
            }
            if (editDTO.getAuditRemark() != null) {
                updateEntity.setAuditRemark(normalizeAuditRemark(editDTO.getAuditRemark()));
            }
        } else if (hasAuditFieldChange) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅待审核网站允许修改审核备注与官方标识");
        }

        updateEntity.setUpdateTime(LocalDateTime.now());

        int affectedRows = websiteMapper.updateById(updateEntity);
        if (affectedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "修改网站失败，请稍后重试");
        }
    }

    /**
     * 删除网站（逻辑删除）
     *
     * @param websiteId 网站ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWebsite(Long websiteId) {
        queryAvailableWebsiteById(websiteId);

        LocalDateTime operationTime = LocalDateTime.now();
        Website updateEntity = new Website();
        updateEntity.setId(websiteId);
        updateEntity.setDeleted(DELETED);
        updateEntity.setStatus(OFFLINE_STATUS);
        updateEntity.setUpdateTime(operationTime);
        updateEntity.setTakedownTime(operationTime);

        int affectedRows = websiteMapper.updateById(updateEntity);
        if (affectedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "网站删除失败，请稍后重试");
        }
    }

    /**
     * 更新网站上架状态
     *
     * @param updateDTO 状态更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWebsiteStatus(AdminWebsiteStatusUpdateDTO updateDTO) {
        Website website = queryAvailableWebsiteById(updateDTO.getWebsiteId());
        if (Objects.equals(updateDTO.getStatus(), ONLINE_STATUS)
                && !Objects.equals(website.getAuditStatus(), AUDIT_APPROVED_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅审核通过的网站可上架");
        }
        if (Objects.equals(website.getStatus(), updateDTO.getStatus())) {
            return;
        }

        LocalDateTime operationTime = LocalDateTime.now();
        Website updateEntity = buildStatusUpdateEntity(website.getId(), updateDTO.getStatus(), operationTime);
        int affectedRows = websiteMapper.updateById(updateEntity);
        if (affectedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "网站状态更新失败，请稍后重试");
        }
    }

    /**
     * 批量更新网站上架状态
     *
     * @param updateDTO 批量状态更新参数
     * @return 实际更新数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateWebsiteStatus(AdminWebsiteBatchStatusUpdateDTO updateDTO) {
        List<Long> normalizedWebsiteIds = updateDTO.getWebsiteIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(normalizedWebsiteIds)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "网站ID列表不能为空");
        }

        List<Website> websiteList = queryAvailableWebsiteByIds(normalizedWebsiteIds);
        if (websiteList.size() != normalizedWebsiteIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在无效的网站ID，无法批量更新状态");
        }

        LocalDateTime operationTime = LocalDateTime.now();
        int updateCount = 0;
        for (Website website : websiteList) {
            if (Objects.equals(updateDTO.getStatus(), ONLINE_STATUS)
                    && !Objects.equals(website.getAuditStatus(), AUDIT_APPROVED_STATUS)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "存在未审核通过的网站，无法批量上架");
            }
            if (Objects.equals(website.getStatus(), updateDTO.getStatus())) {
                continue;
            }

            Website updateEntity = buildStatusUpdateEntity(website.getId(), updateDTO.getStatus(), operationTime);
            int affectedRows = websiteMapper.updateById(updateEntity);
            if (affectedRows != 1) {
                throw new BusinessException(HttpStatus.ERROR, "批量更新网站状态失败，请稍后重试");
            }
            updateCount++;
        }
        return updateCount;
    }

    /**
     * 单条审核网站
     *
     * @param websiteId 网站ID
     * @param auditDTO 审核参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditWebsite(Long websiteId, AdminWebsiteAuditDTO auditDTO) {
        Long adminId = StpAdminUtil.getLoginIdAsLong();
        if (adminId == null || adminId <= 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }

        Website website = queryAvailableWebsiteById(websiteId);
        if (!Objects.equals(website.getAuditStatus(), AUDIT_PENDING_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅待审核网站可操作");
        }

        String normalizedRemark = normalizeAuditRemark(auditDTO.getRemark());
        validateAuditRequest(auditDTO.getAction(), normalizedRemark);

        LocalDateTime operationTime = LocalDateTime.now();
        Website updateEntity = buildAuditUpdateEntity(website, auditDTO.getAction(), normalizedRemark, adminId, operationTime);

        int affectedRows = websiteMapper.updateById(updateEntity);
        if (affectedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "网站审核失败，请稍后重试");
        }

        insertAuditResultMessage(website, updateEntity, operationTime);
    }

    /**
     * 批量审核网站
     *
     * @param batchAuditDTO 批量审核参数
     * @return 审核结果
     */
    @Override
    public AdminWebsiteBatchAuditResultVO batchAuditWebsite(AdminWebsiteBatchAuditDTO batchAuditDTO) {
        Long adminId = StpAdminUtil.getLoginIdAsLong();
        if (adminId == null || adminId <= 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }

        List<Long> normalizedWebsiteIds = batchAuditDTO.getWebsiteIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(normalizedWebsiteIds)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "网站ID列表不能为空");
        }

        String normalizedRemark = normalizeAuditRemark(batchAuditDTO.getRemark());
        validateAuditRequest(batchAuditDTO.getAction(), normalizedRemark);

        List<Website> websiteList = queryAvailableWebsiteByIds(normalizedWebsiteIds);
        Map<Long, Website> websiteMap = websiteList.stream().collect(Collectors.toMap(Website::getId, item -> item));
        List<AdminWebsiteBatchAuditFailItemVO> failItems = new ArrayList<>();
        LocalDateTime operationTime = LocalDateTime.now();
        int successCount = 0;

        for (Long websiteId : normalizedWebsiteIds) {
            Website website = websiteMap.get(websiteId);
            if (website == null) {
                failItems.add(buildFailItem(websiteId, "网站不存在或已删除"));
                continue;
            }
            if (!Objects.equals(website.getAuditStatus(), AUDIT_PENDING_STATUS)) {
                failItems.add(buildFailItem(websiteId, "仅待审核网站可操作"));
                continue;
            }

            Website updateEntity = buildAuditUpdateEntity(website, batchAuditDTO.getAction(), normalizedRemark, adminId, operationTime);
            int affectedRows = websiteMapper.updateById(updateEntity);
            if (affectedRows != 1) {
                failItems.add(buildFailItem(websiteId, "网站审核失败，请稍后重试"));
                continue;
            }

            insertAuditResultMessage(website, updateEntity, operationTime);
            successCount++;
        }

        AdminWebsiteBatchAuditResultVO resultVO = new AdminWebsiteBatchAuditResultVO();
        resultVO.setSuccessCount(successCount);
        resultVO.setFailedCount(failItems.size());
        resultVO.setFailItems(failItems);
        return resultVO;
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

        if (queryDTO.getAuditStatus() != null) {
            queryWrapper.eq(Website::getAuditStatus, queryDTO.getAuditStatus());
        } else if (queryDTO.getAuditBucket() != null) {
            appendAuditBucketCondition(queryWrapper, queryDTO.getAuditBucket());
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
     * 应用审核分组筛选条件
     *
     * @param queryWrapper 查询条件
     * @param auditBucket 审核分组
     */
    private void appendAuditBucketCondition(LambdaQueryWrapper<Website> queryWrapper, Integer auditBucket) {
        if (Objects.equals(auditBucket, AUDIT_BUCKET_PENDING)) {
            queryWrapper.eq(Website::getAuditStatus, AUDIT_PENDING_STATUS);
            return;
        }
        if (Objects.equals(auditBucket, AUDIT_BUCKET_AUDITED)) {
            queryWrapper.in(Website::getAuditStatus, Arrays.asList(AUDIT_APPROVED_STATUS, AUDIT_REJECTED_STATUS));
            return;
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, "审核分组参数不合法");
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
     * 查询分类名称
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
     * 查询提交用户名称
     *
     * @param submitterId 提交用户ID
     * @return 提交用户名称
     */
    private String resolveSubmitterName(Long submitterId) {
        if (submitterId == null || submitterId <= 0) {
            return "";
        }
        UserAccount userAccount = userAccountMapper.selectById(submitterId);
        if (userAccount == null) {
            return "";
        }
        return resolveDisplayName(userAccount.getNickname(), userAccount.getUsername());
    }

    /**
     * 查询审核管理员名称
     *
     * @param auditAdminId 审核管理员ID
     * @return 审核管理员名称
     */
    private String resolveAuditAdminName(Integer auditAdminId) {
        if (auditAdminId == null || auditAdminId <= 0) {
            return "";
        }
        AdminAccount adminAccount = adminAccountMapper.selectById(Long.valueOf(auditAdminId));
        if (adminAccount == null) {
            return "";
        }
        return resolveDisplayName(adminAccount.getNickname(), adminAccount.getUsername());
    }

    /**
     * 解析标签名称列表
     *
     * @param tagsRaw 原始标签文本
     * @return 标签名称列表
     */
    private List<String> resolveTagNameList(String tagsRaw) {
        List<Long> tagIds = parseTagIds(tagsRaw);
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptyList();
        }

        List<Tag> tagList = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .in(Tag::getId, tagIds)
                .eq(Tag::getDeleted, NOT_DELETED));
        if (CollUtil.isEmpty(tagList)) {
            return Collections.emptyList();
        }

        Map<Long, String> tagNameMap = tagList.stream()
                .filter(tag -> tag.getId() != null && StringUtils.hasText(tag.getName()))
                .collect(Collectors.toMap(Tag::getId, Tag::getName, (left, right) -> left));

        return tagIds.stream()
                .map(tagNameMap::get)
                .filter(StringUtils::hasText)
                .toList();
    }

    /**
     * 解析标签ID列表
     *
     * @param tagsRaw 原始标签文本
     * @return 标签ID列表
     */
    private List<Long> parseTagIds(String tagsRaw) {
        if (!StringUtils.hasText(tagsRaw)) {
            return Collections.emptyList();
        }

        return Arrays.stream(tagsRaw.replace('，', ',').split(","))
                .map(tag -> tag.replaceAll("\\s+", ""))
                .filter(StringUtils::hasText)
                .map(this::parseLongSafely)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 安全解析 Long
     *
     * @param value 原始值
     * @return Long 结果
     */
    private Long parseLongSafely(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 解析展示名称
     *
     * @param nickname 昵称
     * @param username 用户名
     * @return 展示名称
     */
    private String resolveDisplayName(String nickname, String username) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        if (StringUtils.hasText(username)) {
            return username.trim();
        }
        return "";
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
     * 查询可操作的网站
     *
     * @param websiteId 网站ID
     * @return 网站信息
     */
    private Website queryAvailableWebsiteById(Long websiteId) {
        Website website = websiteMapper.selectOne(new LambdaQueryWrapper<Website>()
                .eq(Website::getId, websiteId)
                .eq(Website::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (website == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "网站不存在或已删除");
        }
        return website;
    }

    /**
     * 批量查询可操作的网站
     *
     * @param websiteIds 网站ID列表
     * @return 网站列表
     */
    private List<Website> queryAvailableWebsiteByIds(List<Long> websiteIds) {
        if (CollUtil.isEmpty(websiteIds)) {
            return Collections.emptyList();
        }
        return websiteMapper.selectList(new LambdaQueryWrapper<Website>()
                .in(Website::getId, websiteIds)
                .eq(Website::getDeleted, NOT_DELETED));
    }

    /**
     * 构建状态更新实体
     *
     * @param websiteId 网站ID
     * @param status 目标状态
     * @param operationTime 操作时间
     * @return 更新实体
     */
    private Website buildStatusUpdateEntity(Long websiteId, Integer status, LocalDateTime operationTime) {
        Website updateEntity = new Website();
        updateEntity.setId(websiteId);
        updateEntity.setStatus(status);
        updateEntity.setUpdateTime(operationTime);
        if (Objects.equals(status, ONLINE_STATUS)) {
            updateEntity.setShelfTime(operationTime);
        }
        if (Objects.equals(status, OFFLINE_STATUS)) {
            updateEntity.setTakedownTime(operationTime);
        }
        return updateEntity;
    }

    /**
     * 构建审核更新实体
     *
     * @param website 原始网站
     * @param action 审核动作
     * @param remark 审核备注
     * @param adminId 管理员ID
     * @param operationTime 操作时间
     * @return 更新实体
     */
    private Website buildAuditUpdateEntity(Website website, Integer action, String remark, Long adminId, LocalDateTime operationTime) {
        Website updateEntity = new Website();
        updateEntity.setId(website.getId());
        updateEntity.setUpdateTime(operationTime);
        updateEntity.setAuditAdminId(toIntExactAdminId(adminId));

        if (Objects.equals(action, AUDIT_APPROVE_ACTION)) {
            updateEntity.setAuditStatus(AUDIT_APPROVED_STATUS);
            updateEntity.setStatus(ONLINE_STATUS);
            updateEntity.setAuditRemark(StringUtils.hasText(remark) ? remark : AUDIT_APPROVE_REMARK);
            updateEntity.setShelfTime(operationTime);
            return updateEntity;
        }

        updateEntity.setAuditStatus(AUDIT_REJECTED_STATUS);
        updateEntity.setStatus(OFFLINE_STATUS);
        updateEntity.setAuditRemark(remark);
        updateEntity.setTakedownTime(operationTime);
        return updateEntity;
    }

    /**
     * 校验审核参数
     *
     * @param action 审核动作
     * @param remark 备注
     */
    private void validateAuditRequest(Integer action, String remark) {
        if (!Objects.equals(action, AUDIT_APPROVE_ACTION) && !Objects.equals(action, AUDIT_REJECT_ACTION)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "审核动作不合法");
        }
        if (Objects.equals(action, AUDIT_REJECT_ACTION) && !StringUtils.hasText(remark)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "驳回时审核备注不能为空");
        }
    }

    /**
     * 标准化审核备注
     *
     * @param remark 原始备注
     * @return 标准化备注
     */
    private String normalizeAuditRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return "";
        }
        return remark.trim();
    }

    /**
     * 构建批量失败项
     *
     * @param websiteId 网站ID
     * @param reason 失败原因
     * @return 失败项
     */
    private AdminWebsiteBatchAuditFailItemVO buildFailItem(Long websiteId, String reason) {
        AdminWebsiteBatchAuditFailItemVO failItemVO = new AdminWebsiteBatchAuditFailItemVO();
        failItemVO.setWebsiteId(websiteId);
        failItemVO.setReason(reason);
        return failItemVO;
    }

    /**
     * 写入审核结果站内消息
     *
     * @param originWebsite 原始网站
     * @param updateEntity 更新实体
     * @param operationTime 操作时间
     */
    private void insertAuditResultMessage(Website originWebsite, Website updateEntity, LocalDateTime operationTime) {
        if (!Objects.equals(originWebsite.getSource(), 1)) {
            return;
        }
        if (originWebsite.getSubmitterId() == null || originWebsite.getSubmitterId() <= 0) {
            return;
        }

        SystemMessage message = new SystemMessage();
        message.setUserId(originWebsite.getSubmitterId());
        message.setType(DEFAULT_AUDIT_MESSAGE_TYPE);
        message.setIsRead(UNREAD_MESSAGE);
        message.setRelatedId(originWebsite.getId());
        message.setCreateTime(operationTime);
        message.setTitle("网站投稿审核结果通知");

        if (Objects.equals(updateEntity.getAuditStatus(), AUDIT_APPROVED_STATUS)) {
            message.setContent("你投稿的网站《" + originWebsite.getName() + "》已审核通过并自动上架。");
        } else {
            message.setContent("你投稿的网站《" + originWebsite.getName() + "》未通过审核，原因：" + updateEntity.getAuditRemark());
        }

        systemMessageMapper.insert(message);
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
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类已禁用，无法保存网站");
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
     * 校验编辑参数中的敏感词
     *
     * @param editDTO 编辑参数
     */
    private void validateSensitiveFields(AdminWebsiteEditDTO editDTO) {
        checkSensitiveField("网站名称", normalizeRequiredText(editDTO.getName()));
        checkSensitiveField("一句话简介", normalizeOptionalText(editDTO.getSummary()));
        checkSensitiveField("详细描述", normalizeOptionalText(editDTO.getDescription()));
        checkSensitiveField("标签", normalizeTags(editDTO.getTags()));
        checkSensitiveField("审核备注", normalizeAuditRemark(editDTO.getAuditRemark()));
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
