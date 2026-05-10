package com.yyyouth.service.user.userstats.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.pojo.user.UserFolder;
import com.yyyouth.model.vo.userstats.ProfilePublicVO;
import com.yyyouth.model.vo.userstats.PublicFolderChildVO;
import com.yyyouth.model.vo.userstats.PublicFolderChildrenVO;
import com.yyyouth.model.vo.userstats.PublicFolderItemVO;
import com.yyyouth.model.vo.userstats.PublicFolderTreeVO;
import com.yyyouth.model.vo.userstats.PublicFolderWebsiteVO;
import com.yyyouth.service.mapper.user.UserCollectMapper;
import com.yyyouth.service.mapper.user.UserFolderMapper;
import com.yyyouth.service.redis.RedisCache;
import com.yyyouth.service.user.userstats.ProfilePublicService;
import com.yyyouth.service.user.userstats.PublicFolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开主页收藏夹查询实现（user-15 公开收藏夹）。
 *
 * <p>隐私链路通过 {@link ProfilePublicService#getPublicProfile} 一站式解析 username → userId、
 * 校验 visibility（PRIVATE / LOGGED 自抛错）、并取得 isOwn / showCollections。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublicFolderServiceImpl implements PublicFolderService {

    private static final long TOP_TTL_SECONDS = 300L;
    private static final long TOP_JITTER_SECONDS = 60L;
    private static final long CHILDREN_TTL_SECONDS = 180L;
    private static final long CHILDREN_JITTER_SECONDS = 30L;
    private static final long TREE_TTL_SECONDS = 300L;
    private static final long TREE_JITTER_SECONDS = 60L;

    private static final long TOP_LEVEL_PARENT_ID = 0L;
    private static final int FOLDER_STATUS_NORMAL = 1;
    private static final int NOT_DELETED = 0;
    private static final int NOT_HIDE = 0;
    private static final int IS_PUBLIC = 1;

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 12;

    private final ProfilePublicService profilePublicService;
    private final UserFolderMapper userFolderMapper;
    private final UserCollectMapper userCollectMapper;
    private final RedisCache redisCache;

    @Override
    public List<PublicFolderItemVO> getTopFolders(String username, Long currentUserId, int limit) {
        ResolvedAccess access = resolveAccess(username, currentUserId);
        Long targetUserId = access.targetUserId;
        boolean isOwn = access.isOwn;
        int safeLimit = Math.max(1, Math.min(limit, 50));

        String key = RedisConstant.PUBLIC_FOLDER_TOP_PREFIX + targetUserId
                + ":top:" + safeLimit + ":" + (isOwn ? "own" : "visitor");

        return redisCache.getOrLoad(key,
                new TypeReference<List<PublicFolderItemVO>>() { },
                TOP_TTL_SECONDS, TOP_JITTER_SECONDS,
                () -> loadTopFolders(targetUserId, isOwn, safeLimit));
    }

    @Override
    public PublicFolderChildrenVO getFolderChildren(String username, Long currentUserId,
                                                    Long folderId, int pageNum, int pageSize) {
        if (folderId == null || folderId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "文件夹 ID 非法");
        }
        int safePageNum = Math.max(1, pageNum);
        int safePageSize = pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE ? DEFAULT_PAGE_SIZE : pageSize;

        ResolvedAccess access = resolveAccess(username, currentUserId);
        Long targetUserId = access.targetUserId;
        boolean isOwn = access.isOwn;

        String key = RedisConstant.PUBLIC_FOLDER_CHILDREN_PREFIX + targetUserId
                + ":" + folderId + ":" + safePageNum + ":" + safePageSize
                + ":" + (isOwn ? "own" : "visitor");

        return redisCache.getOrLoad(key, PublicFolderChildrenVO.class,
                CHILDREN_TTL_SECONDS, CHILDREN_JITTER_SECONDS,
                () -> loadFolderChildren(targetUserId, isOwn, folderId, safePageNum, safePageSize));
    }

    @Override
    public PublicFolderTreeVO getPublicFolderTree(String username, Long currentUserId) {
        ResolvedAccess access = resolveAccess(username, currentUserId);
        Long targetUserId = access.targetUserId;
        boolean isOwn = access.isOwn;

        String key = RedisConstant.PUBLIC_FOLDER_TREE_PREFIX + targetUserId
                + ":" + (isOwn ? "own" : "visitor");

        return redisCache.getOrLoad(key, PublicFolderTreeVO.class,
                TREE_TTL_SECONDS, TREE_JITTER_SECONDS,
                () -> loadPublicFolderTree(targetUserId, isOwn));
    }

    // ===================== private =====================

    /**
     * 复用 {@link ProfilePublicService#getPublicProfile} 一站式：解析 username + visibility 校验 + 取 isOwn / showCollections。
     * 非本人且 showCollections=0 → PROFILE_PRIVATE。
     */
    private ResolvedAccess resolveAccess(String username, Long currentUserId) {
        ProfilePublicVO publicVO = profilePublicService.getPublicProfile(username, currentUserId);
        if (publicVO == null || publicVO.getUser() == null || publicVO.getUser().getId() == null) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(),
                    AuthErrorCode.USER_DISABLED.getMessage());
        }
        boolean isOwn = Boolean.TRUE.equals(publicVO.getIsOwn());
        Integer showCollections = publicVO.getPrivacy() != null
                ? publicVO.getPrivacy().getShowCollections() : null;
        if (!isOwn && (showCollections == null || showCollections == 0)) {
            throw new BusinessException(AuthErrorCode.PROFILE_PRIVATE.getCode(),
                    AuthErrorCode.PROFILE_PRIVATE.getMessage());
        }
        return new ResolvedAccess(publicVO.getUser().getId(), isOwn);
    }

    private List<PublicFolderItemVO> loadTopFolders(Long userId, boolean isOwn, int limit) {
        LambdaQueryWrapper<UserFolder> wrapper = new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getParentId, TOP_LEVEL_PARENT_ID)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .orderByAsc(UserFolder::getSort)
                .orderByAsc(UserFolder::getId);
        if (!isOwn) {
            wrapper.eq(UserFolder::getIsHide, NOT_HIDE)
                   .eq(UserFolder::getIsPublic, IS_PUBLIC);
        }
        List<UserFolder> tops = userFolderMapper.selectList(wrapper);
        if (tops.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> topIds = tops.stream().map(UserFolder::getId).collect(Collectors.toSet());
        Map<Long, Long> childrenCountMap = countVisibleChildren(userId, isOwn, topIds);

        List<PublicFolderItemVO> list = new ArrayList<>(tops.size());
        for (UserFolder f : tops) {
            int childCount = childrenCountMap.getOrDefault(f.getId(), 0L).intValue();
            int siteCount = f.getWebsiteCount() == null ? 0 : f.getWebsiteCount();
            // 他人视图：过滤"空夹"（无站 & 无可见子）；本人视图保留所有
            if (!isOwn && siteCount == 0 && childCount == 0) {
                continue;
            }
            list.add(PublicFolderItemVO.builder()
                    .id(f.getId())
                    .name(f.getName())
                    .icon(f.getIcon())
                    .color(f.getColor())
                    .parentId(f.getParentId())
                    .sort(f.getSort())
                    .websiteCount(siteCount)
                    .childrenCount(childCount)
                    .build());
            if (list.size() >= limit) {
                break;
            }
        }
        return list;
    }

    /**
     * 聚合给定父文件夹下"可见"子文件夹数量。一次 SQL 解决 N+1。
     */
    private Map<Long, Long> countVisibleChildren(Long userId, boolean isOwn, Set<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<UserFolder> w = new LambdaQueryWrapper<UserFolder>()
                .select(UserFolder::getId, UserFolder::getParentId)
                .eq(UserFolder::getUserId, userId)
                .in(UserFolder::getParentId, parentIds)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED);
        if (!isOwn) {
            w.eq(UserFolder::getIsHide, NOT_HIDE)
             .eq(UserFolder::getIsPublic, IS_PUBLIC);
        }
        List<UserFolder> children = userFolderMapper.selectList(w);
        Map<Long, Long> map = new LinkedHashMap<>();
        for (UserFolder c : children) {
            map.merge(c.getParentId(), 1L, Long::sum);
        }
        return map;
    }

    private PublicFolderChildrenVO loadFolderChildren(Long userId, boolean isOwn, Long folderId,
                                                      int pageNum, int pageSize) {
        // 1. 校验 folder 归属 + 可见性
        LambdaQueryWrapper<UserFolder> ownerWrapper = new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getId, folderId)
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED);
        if (!isOwn) {
            ownerWrapper.eq(UserFolder::getIsHide, NOT_HIDE)
                        .eq(UserFolder::getIsPublic, IS_PUBLIC);
        }
        UserFolder folder = userFolderMapper.selectOne(ownerWrapper.last("limit 1"));
        if (folder == null) {
            // 不存在或对访客不可见 → 与 PRIVATE 同错误码避免存在性枚举
            throw new BusinessException(AuthErrorCode.PROFILE_PRIVATE.getCode(),
                    AuthErrorCode.PROFILE_PRIVATE.getMessage());
        }

        // 2. 子文件夹（无分页，预期单层数量小）
        LambdaQueryWrapper<UserFolder> childWrapper = new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getParentId, folderId)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .orderByAsc(UserFolder::getSort)
                .orderByAsc(UserFolder::getId);
        if (!isOwn) {
            childWrapper.eq(UserFolder::getIsHide, NOT_HIDE)
                        .eq(UserFolder::getIsPublic, IS_PUBLIC);
        }
        List<PublicFolderChildVO> subFolders = userFolderMapper.selectList(childWrapper).stream()
                .map(c -> PublicFolderChildVO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .icon(c.getIcon())
                        .color(c.getColor())
                        .parentId(c.getParentId())
                        .websiteCount(c.getWebsiteCount() == null ? 0 : c.getWebsiteCount())
                        .build())
                .collect(Collectors.toList());

        // 3. 直属网站分页
        Page<PublicFolderWebsiteVO> page = new Page<>(pageNum, pageSize);
        IPage<PublicFolderWebsiteVO> result = userCollectMapper.selectPublicWebsitesByFolder(page, userId, folderId);

        return PublicFolderChildrenVO.builder()
                .subFolders(subFolders)
                .websites(PublicFolderChildrenVO.WebsitePage.builder()
                        .list(result.getRecords())
                        .total(result.getTotal())
                        .pageNum(pageNum)
                        .pageSize(pageSize)
                        .build())
                .build();
    }

    private PublicFolderTreeVO loadPublicFolderTree(Long userId, boolean isOwn) {
        LambdaQueryWrapper<UserFolder> w = new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .orderByAsc(UserFolder::getSort)
                .orderByAsc(UserFolder::getId);
        if (!isOwn) {
            w.eq(UserFolder::getIsHide, NOT_HIDE)
             .eq(UserFolder::getIsPublic, IS_PUBLIC);
        }
        List<UserFolder> all = userFolderMapper.selectList(w);

        Map<Long, PublicFolderTreeVO.TreeNode> nodeMap = new LinkedHashMap<>();
        for (UserFolder f : all) {
            nodeMap.put(f.getId(), PublicFolderTreeVO.TreeNode.builder()
                    .id(f.getId())
                    .name(f.getName())
                    .icon(f.getIcon())
                    .color(f.getColor())
                    .parentId(f.getParentId())
                    .sort(f.getSort())
                    .websiteCount(f.getWebsiteCount() == null ? 0 : f.getWebsiteCount())
                    .children(new ArrayList<>())
                    .build());
        }

        List<PublicFolderTreeVO.TreeNode> roots = new ArrayList<>();
        for (PublicFolderTreeVO.TreeNode node : nodeMap.values()) {
            Long pid = node.getParentId();
            if (pid == null || pid == TOP_LEVEL_PARENT_ID) {
                roots.add(node);
            } else {
                PublicFolderTreeVO.TreeNode parent = nodeMap.get(pid);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else if (isOwn) {
                    // 本人：父对自己不可见（理论不存在）容错挂到 roots；他人：整支挂掉
                    roots.add(node);
                }
            }
        }
        return PublicFolderTreeVO.builder().roots(roots).build();
    }

    /**
     * 解析后的访问上下文。
     */
    private record ResolvedAccess(Long targetUserId, boolean isOwn) { }
}
