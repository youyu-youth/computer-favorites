package com.yyyouth.service.user.collect.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserCollectCreateDTO;
import com.yyyouth.model.dto.user.UserCollectPageDTO;
import com.yyyouth.model.pojo.user.UserCollect;
import com.yyyouth.model.pojo.user.UserFolder;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.vo.user.UserCollectItemVO;
import com.yyyouth.model.vo.user.UserCollectPageVO;
import com.yyyouth.model.vo.user.UserCollectStatsVO;
import com.yyyouth.model.vo.user.UserWebsiteTagItemVO;
import com.yyyouth.service.mapper.user.UserCollectMapper;
import com.yyyouth.service.mapper.user.UserFolderMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.collect.UserCollectService;
import com.yyyouth.service.user.website.support.UserWebsiteTagSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserCollectServiceImpl implements UserCollectService {

    private static final int NOT_DELETED = 0;
    private static final int ENABLED_STATUS = 1;
    private static final int FOLDER_STATUS_NORMAL = 1;
    private static final long TOP_LEVEL_PARENT_ID = 0L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserCollectMapper userCollectMapper;
    private final UserFolderMapper userFolderMapper;
    private final WebsiteMapper websiteMapper;
    private final UserWebsiteTagSupport userWebsiteTagSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long collect(UserCollectCreateDTO createDTO) {
        Long userId = getCurrentUserId();

        Website website = websiteMapper.selectOne(new LambdaQueryWrapper<Website>()
                .eq(Website::getId, createDTO.getWebsiteId())
                .eq(Website::getStatus, ENABLED_STATUS)
                .eq(Website::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (website == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "网站不存在或已下架");
        }

        UserCollect existing = userCollectMapper.selectOne(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getWebsiteId, createDTO.getWebsiteId())
                .last("limit 1"));
        if (existing != null) {
            Long newFolderId = createDTO.getFolderId();
            if (newFolderId == null || newFolderId <= 0) {
                newFolderId = getDefaultFolderId(userId);
            }
            Long oldFolderId = existing.getFolderId();
            if (Objects.equals(oldFolderId, newFolderId)) {
                log.info("收藏文件夹未变更，userId={}, websiteId={}, folderId={}", userId, createDTO.getWebsiteId(), oldFolderId);
                return existing.getId();
            }
            validateFolderOwnership(userId, newFolderId);
            userCollectMapper.update(null, new LambdaUpdateWrapper<UserCollect>()
                    .eq(UserCollect::getId, existing.getId())
                    .set(UserCollect::getFolderId, newFolderId));
            if (oldFolderId != null && oldFolderId > 0) {
                decrementFolderWebsiteCount(oldFolderId);
            }
            incrementFolderWebsiteCount(newFolderId);
            log.info("收藏移夹成功，userId={}, websiteId={}, oldFolderId={}, newFolderId={}",
                    userId, createDTO.getWebsiteId(), oldFolderId, newFolderId);
            return existing.getId();
        }

        Long folderId = createDTO.getFolderId();
        if (folderId != null && folderId > 0) {
            validateFolderOwnership(userId, folderId);
        } else {
            Long defaultFolderId = getDefaultFolderId(userId);
            folderId = defaultFolderId;
        }

        UserCollect collect = UserCollect.builder()
                .userId(userId)
                .websiteId(createDTO.getWebsiteId())
                .folderId(folderId)
                .build();

        try {
            int inserted = userCollectMapper.insert(collect);
            if (inserted != 1 || collect.getId() == null) {
                throw new BusinessException(HttpStatus.ERROR, "收藏失败，请稍后重试");
            }

            incrementFolderWebsiteCount(folderId);
            incrementWebsiteCollectCount(createDTO.getWebsiteId());

            log.info("收藏成功，userId={}, websiteId={}, folderId={}, collectId={}",
                    userId, createDTO.getWebsiteId(), folderId, collect.getId());

            return collect.getId();
        } catch (DuplicateKeyException ex) {
            log.info("收藏并发冲突，userId={}, websiteId={}", userId, createDTO.getWebsiteId());
            UserCollect existingRecord = userCollectMapper.selectOne(new LambdaQueryWrapper<UserCollect>()
                    .eq(UserCollect::getUserId, userId)
                    .eq(UserCollect::getWebsiteId, createDTO.getWebsiteId())
                    .last("limit 1"));
            return existingRecord != null ? existingRecord.getId() : null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCollect(Long websiteId) {
        Long userId = getCurrentUserId();

        UserCollect existing = userCollectMapper.selectOne(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getWebsiteId, websiteId)
                .last("limit 1"));
        if (existing == null) {
            log.info("取消收藏已忽略，记录不存在，userId={}, websiteId={}", userId, websiteId);
            return;
        }

        int deleted = userCollectMapper.deleteById(existing.getId());
        if (deleted != 1) {
            throw new BusinessException(HttpStatus.ERROR, "取消收藏失败，请稍后重试");
        }

        if (existing.getFolderId() != null && existing.getFolderId() > 0) {
            decrementFolderWebsiteCount(existing.getFolderId());
        }
        decrementWebsiteCollectCount(websiteId);

        log.info("取消收藏成功，userId={}, websiteId={}, collectId={}", userId, websiteId, existing.getId());
    }

    @Override
    public UserCollectPageVO pageCollectList(UserCollectPageDTO pageDTO) {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<UserCollect> wrapper = new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .orderByDesc(UserCollect::getCreateTime);

        if (pageDTO.getFolderId() != null && pageDTO.getFolderId() > 0) {
            wrapper.eq(UserCollect::getFolderId, pageDTO.getFolderId());
        }

        Page<UserCollect> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<UserCollect> result = userCollectMapper.selectPage(page, wrapper);

        List<Long> websiteIds = result.getRecords().stream()
                .map(UserCollect::getWebsiteId)
                .distinct()
                .toList();
        Map<Long, Website> websiteMap = websiteIds.isEmpty() ? Map.of() :
                websiteMapper.selectList(new LambdaQueryWrapper<Website>()
                                .in(Website::getId, websiteIds)
                                .eq(Website::getDeleted, NOT_DELETED))
                        .stream().collect(Collectors.toMap(Website::getId, w -> w, (a, b) -> a));

        Set<Long> tagIds = websiteMap.values().stream()
                .flatMap(w -> userWebsiteTagSupport.parseTagIds(w.getTags()).stream())
                .collect(Collectors.toSet());
        Map<Long, UserWebsiteTagItemVO> tagItemMap = userWebsiteTagSupport.buildTagItemMap(tagIds);

        List<UserCollectItemVO> items = new ArrayList<>();
        for (UserCollect collect : result.getRecords()) {
            UserCollectItemVO item = buildCollectItemVO(collect, websiteMap, tagItemMap);
            if (item != null) {
                if (StringUtils.hasText(pageDTO.getKeyword())) {
                    String keyword = pageDTO.getKeyword().trim().toLowerCase();
                    boolean nameMatch = item.getWebsiteName() != null && item.getWebsiteName().toLowerCase().contains(keyword);
                    boolean summaryMatch = item.getWebsiteSummary() != null && item.getWebsiteSummary().toLowerCase().contains(keyword);
                    boolean tagMatch = item.getWebsiteTags() != null && item.getWebsiteTags().stream()
                            .anyMatch(t -> t.getName() != null && t.getName().toLowerCase().contains(keyword));
                    if (!nameMatch && !summaryMatch && !tagMatch) {
                        continue;
                    }
                }
                items.add(item);
            }
        }

        int totalPages = (int) Math.ceil((double) result.getTotal() / pageDTO.getPageSize());

        return UserCollectPageVO.builder()
                .records(items)
                .total(result.getTotal())
                .pageNum(pageDTO.getPageNum())
                .pageSize(pageDTO.getPageSize())
                .totalPages(totalPages)
                .build();
    }

    @Override
    public UserCollectStatsVO getCollectStats() {
        Long userId = getCurrentUserId();

        Long collectCount = userCollectMapper.selectCount(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId));

        Long folderCount = userFolderMapper.selectCount(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL));

        return UserCollectStatsVO.builder()
                .collectCount(collectCount != null ? collectCount : 0L)
                .folderCount(folderCount != null ? folderCount : 0L)
                .build();
    }

    private Long getCurrentUserId() {
        StpUtil.checkLogin();
        return StpUtil.getLoginIdAsLong();
    }

    private void validateFolderOwnership(Long userId, Long folderId) {
        UserFolder folder = userFolderMapper.selectOne(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getId, folderId)
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (folder == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "收藏夹不存在或不可用");
        }
    }

    private Long getDefaultFolderId(Long userId) {
        UserFolder defaultFolder = userFolderMapper.selectOne(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getIsDefault, 1)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .last("limit 1"));
        if (defaultFolder != null) {
            return defaultFolder.getId();
        }

        UserFolder anyFolder = userFolderMapper.selectOne(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .orderByAsc(UserFolder::getSort)
                .last("limit 1"));
        if (anyFolder != null) {
            return anyFolder.getId();
        }

        throw new BusinessException(HttpStatus.BAD_REQUEST, "请先创建收藏夹");
    }

    private void incrementFolderWebsiteCount(Long folderId) {
        userFolderMapper.update(null, new LambdaUpdateWrapper<UserFolder>()
                .eq(UserFolder::getId, folderId)
                .gt(UserFolder::getWebsiteCount, -1)
                .setSql("website_count = website_count + 1"));
    }

    private void decrementFolderWebsiteCount(Long folderId) {
        userFolderMapper.update(null, new LambdaUpdateWrapper<UserFolder>()
                .eq(UserFolder::getId, folderId)
                .gt(UserFolder::getWebsiteCount, 0)
                .setSql("website_count = website_count - 1"));
    }

    /**
     * 递增网站收藏量
     *
     * @param websiteId 网站ID
     */
    private void incrementWebsiteCollectCount(Long websiteId) {
        websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                .eq(Website::getId, websiteId)
                .gt(Website::getCollectCount, -1)
                .setSql("collect_count = collect_count + 1"));
    }

    /**
     * 递减网站收藏量
     *
     * @param websiteId 网站ID
     */
    private void decrementWebsiteCollectCount(Long websiteId) {
        websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                .eq(Website::getId, websiteId)
                .gt(Website::getCollectCount, 0)
                .setSql("collect_count = collect_count - 1"));
    }

    private UserCollectItemVO buildCollectItemVO(UserCollect collect, Map<Long, Website> websiteMap, Map<Long, UserWebsiteTagItemVO> tagItemMap) {
        Website website = websiteMap.get(collect.getWebsiteId());
        if (website == null) {
            return null;
        }

        String folderName = null;
        if (collect.getFolderId() != null && collect.getFolderId() > 0) {
            UserFolder folder = userFolderMapper.selectOne(new LambdaQueryWrapper<UserFolder>()
                    .eq(UserFolder::getId, collect.getFolderId())
                    .select(UserFolder::getName)
                    .last("limit 1"));
            if (folder != null) {
                folderName = folder.getName();
            }
        }

        return UserCollectItemVO.builder()
                .id(collect.getId())
                .websiteId(website.getId())
                .websiteName(website.getName())
                .websiteUrl(website.getUrl())
                .websiteIcon(website.getIcon())
                .websiteSummary(website.getSummary())
                .websiteTags(userWebsiteTagSupport.buildWebsiteTagItems(website.getTags(), tagItemMap))
                .likeCount(website.getLikeCount())
                .collectTime(collect.getCreateTime() != null ? collect.getCreateTime().format(DATE_FORMATTER) : null)
                .folderId(collect.getFolderId())
                .folderName(folderName)
                .build();
    }
}
