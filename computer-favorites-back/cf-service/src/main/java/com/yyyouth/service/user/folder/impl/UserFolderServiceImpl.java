package com.yyyouth.service.user.folder.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserFolderCreateDTO;
import com.yyyouth.model.dto.user.UserFolderUpdateDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.UserCollect;
import com.yyyouth.model.pojo.user.UserFolder;
import com.yyyouth.model.vo.user.UserFolderCreateVO;
import com.yyyouth.model.vo.user.UserFolderOptionsVO;
import com.yyyouth.model.vo.user.UserFolderTreeVO;
import com.yyyouth.service.mapper.user.UserCollectMapper;
import com.yyyouth.service.config.redis.RedisCache;
import com.yyyouth.service.mapper.user.UserFolderMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.common.AuthContext;
import com.yyyouth.service.user.folder.UserFolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏文件夹服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserFolderServiceImpl implements UserFolderService {

    private static final int NOT_DELETED = 0;
    private static final int ENABLED_STATUS = 1;
    private static final int FOLDER_STATUS_NORMAL = 1;
    private static final int NOT_DEFAULT_FOLDER = 0;
    private static final int INITIAL_WEBSITE_COUNT = 0;
    private static final int INITIAL_SORT = 0;
    private static final long TOP_LEVEL_PARENT_ID = 0L;
    private static final int MAX_FOLDER_DEPTH = 5;
    private static final int MIN_FOLDER_DEPTH = 1;
    private static final int NOT_HIDE = 0;
    private static final int IS_HIDE = 1;
    private static final int NOT_PUBLIC = 0;
    private static final int IS_PUBLIC = 1;
    private static final int IS_DEFAULT = 1;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserFolderMapper userFolderMapper;
    private final UserAccountMapper userAccountMapper;
    private final RedisCache redisCache;
    private final UserCollectMapper userCollectMapper;
    private final AuthContext authContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserFolderCreateVO createFolder(UserFolderCreateDTO createDTO) {
        Long userId = getCurrentUserId();

        String normalizedName = normalizeFolderName(createDTO.getName());
        validateFolderNameDuplication(userId, createDTO.getParentId(), normalizedName);

        if (createDTO.getParentId() != null && createDTO.getParentId() > TOP_LEVEL_PARENT_ID) {
            validateParentFolder(userId, createDTO.getParentId());
            validateFolderDepth(userId, createDTO.getParentId());
        }

        UserFolder folder = UserFolder.builder()
                .userId(userId)
                .name(normalizedName)
                .icon(normalizeOptionalText(createDTO.getIcon()))
                .color(createDTO.getColor())
                .parentId(createDTO.getParentId() != null ? createDTO.getParentId() : TOP_LEVEL_PARENT_ID)
                .sort(createDTO.getSort() != null ? createDTO.getSort() : INITIAL_SORT)
                .websiteCount(INITIAL_WEBSITE_COUNT)
                .isHide(NOT_HIDE)
                .isDefault(NOT_DEFAULT_FOLDER)
                .status(FOLDER_STATUS_NORMAL)
                .deleted(NOT_DELETED)
                .build();

        int insertedRows = userFolderMapper.insert(folder);
        if (insertedRows != 1 || folder.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "创建收藏夹失败，请稍后重试");
        }

        log.info("用户收藏夹创建成功，userId={}, folderId={}, name={}, parentId={}",
                userId, folder.getId(), normalizedName, folder.getParentId());

        evictPublicFolderCache(userId);

        return UserFolderCreateVO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .icon(folder.getIcon())
                .color(folder.getColor())
                .parentId(folder.getParentId())
                .sort(folder.getSort())
                .build();
    }

    @Override
    public List<UserFolderTreeVO> getFolderTree() {
        Long userId = getCurrentUserId();

        List<UserFolder> allFolders = userFolderMapper.selectList(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .orderByAsc(UserFolder::getSort)
                .orderByAsc(UserFolder::getId));

        List<UserFolderTreeVO> voList = allFolders.stream()
                .map(this::toTreeVO)
                .collect(Collectors.toList());

        return buildTree(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(Long folderId, UserFolderUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();

        UserFolder folder = getOwnedFolder(userId, folderId);

        if (StringUtils.hasText(updateDTO.getName())) {
            String normalizedName = normalizeFolderName(updateDTO.getName());
            if (!normalizedName.equals(folder.getName())) {
                validateFolderNameDuplication(userId, folder.getParentId(), normalizedName);
                folder.setName(normalizedName);
            }
        }
        if (updateDTO.getIcon() != null) {
            folder.setIcon(normalizeOptionalText(updateDTO.getIcon()));
        }
        if (updateDTO.getColor() != null) {
            folder.setColor(updateDTO.getColor());
        }
        if (updateDTO.getSort() != null) {
            folder.setSort(updateDTO.getSort());
        }

        int updated = userFolderMapper.updateById(folder);
        if (updated != 1) {
            throw new BusinessException(HttpStatus.ERROR, "更新收藏夹失败，请稍后重试");
        }

        log.info("收藏夹更新成功，userId={}, folderId={}", userId, folderId);
        evictPublicFolderCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long folderId) {
        Long userId = getCurrentUserId();

        UserFolder folder = getOwnedFolder(userId, folderId);

        if (folder.getIsDefault() != null && folder.getIsDefault() == IS_DEFAULT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "默认收藏夹不可删除");
        }

        Long childCount = userFolderMapper.selectCount(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getParentId, folderId)
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getDeleted, NOT_DELETED));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该收藏夹下存在子文件夹，不可删除");
        }

        Long collectCount = userCollectMapper.selectCount(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getFolderId, folderId)
                .eq(UserCollect::getUserId, userId));
        if (collectCount != null && collectCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该收藏夹下存在收藏网站，请先移除收藏后再删除");
        }

        int deleted = userFolderMapper.update(null, new LambdaUpdateWrapper<UserFolder>()
                .eq(UserFolder::getId, folderId)
                .set(UserFolder::getDeleted, 1));
        if (deleted != 1) {
            throw new BusinessException(HttpStatus.ERROR, "删除收藏夹失败，请稍后重试");
        }

        log.info("收藏夹删除成功，userId={}, folderId={}", userId, folderId);
        evictPublicFolderCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFolderHide(Long folderId, boolean isHide) {
        Long userId = getCurrentUserId();

        UserFolder folder = getOwnedFolder(userId, folderId);

        int targetHide = isHide ? IS_HIDE : NOT_HIDE;
        if (folder.getIsHide() != null && folder.getIsHide() == targetHide) {
            return;
        }

        int updated = userFolderMapper.update(null, new LambdaUpdateWrapper<UserFolder>()
                .eq(UserFolder::getId, folderId)
                .set(UserFolder::getIsHide, targetHide));
        if (updated != 1) {
            throw new BusinessException(HttpStatus.ERROR, "操作失败，请稍后重试");
        }

        log.info("收藏夹隐藏状态变更，userId={}, folderId={}, isHide={}", userId, folderId, isHide);
        evictPublicFolderCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFolderPublic(Long folderId, boolean isPublic) {
        Long userId = getCurrentUserId();

        UserFolder folder = getOwnedFolder(userId, folderId);

        int targetPublic = isPublic ? IS_PUBLIC : NOT_PUBLIC;
        if (folder.getIsPublic() != null && folder.getIsPublic() == targetPublic) {
            return;
        }

        int updated = userFolderMapper.update(null, new LambdaUpdateWrapper<UserFolder>()
                .eq(UserFolder::getId, folderId)
                .set(UserFolder::getIsPublic, targetPublic));
        if (updated != 1) {
            throw new BusinessException(HttpStatus.ERROR, "操作失败，请稍后重试");
        }

        log.info("收藏夹对外可见性变更，userId={}, folderId={}, isPublic={}", userId, folderId, isPublic);
        evictPublicFolderCache(userId);
    }

    @Override
    public List<UserFolderOptionsVO> getFolderOptions() {
        Long userId = getCurrentUserId();

        List<UserFolder> folders = userFolderMapper.selectList(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .eq(UserFolder::getIsHide, NOT_HIDE)
                .orderByAsc(UserFolder::getSort)
                .orderByAsc(UserFolder::getId));

        return folders.stream()
                .map(f -> UserFolderOptionsVO.builder()
                        .id(f.getId())
                        .name(f.getName())
                        .icon(f.getIcon())
                        .color(f.getColor())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean verifyPassword(String password) {
        Long userId = getCurrentUserId();

        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getStatus, ENABLED_STATUS)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户未登录或账号不可用");
        }

        String encodedPassword = userAccount.getPasswordHash();
        if (!StringUtils.hasText(encodedPassword)) {
            encodedPassword = userAccount.getPassword();
        }

        if (!StringUtils.hasText(encodedPassword)) {
            throw new BusinessException(HttpStatus.ERROR, "账户密码信息异常");
        }

        return PASSWORD_ENCODER.matches(password, encodedPassword);
    }

    private Long getCurrentUserId() {
        return authContext.getCurrentUserId();
    }

    /**
     * 清空该用户的公开收藏夹相关缓存（顶层 / 子项 / 全量树）。
     * 任何写入点（create/update/delete/toggleHide/togglePublic）都应调用。
     * 仅 log warn，不影响主流程。
     */
    private void evictPublicFolderCache(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redisCache.evictByPattern(RedisConstant.PUBLIC_FOLDER_TOP_PREFIX + userId + ":*");
            redisCache.evictByPattern(RedisConstant.PUBLIC_FOLDER_CHILDREN_PREFIX + userId + ":*");
            redisCache.evict(RedisConstant.PUBLIC_FOLDER_TREE_PREFIX + userId);
        } catch (Exception e) {
            log.warn("[public-folder] evict cache fail userId={}, err={}", userId, e.getMessage());
        }
    }

    private UserFolder getOwnedFolder(Long userId, Long folderId) {
        UserFolder folder = userFolderMapper.selectOne(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getId, folderId)
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (folder == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "收藏夹不存在或不可用");
        }
        return folder;
    }

    private UserFolderTreeVO toTreeVO(UserFolder folder) {
        return UserFolderTreeVO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .icon(folder.getIcon())
                .color(folder.getColor())
                .parentId(folder.getParentId())
                .sort(folder.getSort())
                .websiteCount(folder.getWebsiteCount())
                .isHide(folder.getIsHide())
                .isPublic(folder.getIsPublic())
                .isDefault(folder.getIsDefault())
                .children(new ArrayList<>())
                .build();
    }

    private List<UserFolderTreeVO> buildTree(List<UserFolderTreeVO> allNodes) {
        Map<Long, UserFolderTreeVO> nodeMap = new LinkedHashMap<>();
        for (UserFolderTreeVO node : allNodes) {
            nodeMap.put(node.getId(), node);
        }

        List<UserFolderTreeVO> roots = new ArrayList<>();
        for (UserFolderTreeVO node : allNodes) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == TOP_LEVEL_PARENT_ID) {
                roots.add(node);
            } else {
                UserFolderTreeVO parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }

        return roots;
    }

    private String normalizeFolderName(String name) {
        String normalized = normalizeOptionalText(name);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "文件夹名称不能为空");
        }
        if (normalized.length() > 50) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "文件夹名称不能超过50个字符");
        }
        return normalized;
    }

    private void validateFolderNameDuplication(Long userId, Long parentId, String name) {
        Long effectiveParentId = parentId != null ? parentId : TOP_LEVEL_PARENT_ID;
        Long count = userFolderMapper.selectCount(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getParentId, effectiveParentId)
                .eq(UserFolder::getName, name)
                .eq(UserFolder::getDeleted, NOT_DELETED));
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "同一层级下已存在同名文件夹");
        }
    }

    private void validateParentFolder(Long userId, Long parentId) {
        UserFolder parentFolder = userFolderMapper.selectOne(new LambdaQueryWrapper<UserFolder>()
                .eq(UserFolder::getId, parentId)
                .eq(UserFolder::getUserId, userId)
                .eq(UserFolder::getStatus, FOLDER_STATUS_NORMAL)
                .eq(UserFolder::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (parentFolder == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "父文件夹不存在或不可用");
        }
    }

    private void validateFolderDepth(Long userId, Long parentId) {
        int depth = MIN_FOLDER_DEPTH;
        Long currentParentId = parentId;

        while (currentParentId != null && currentParentId > TOP_LEVEL_PARENT_ID) {
            depth++;
            if (depth > MAX_FOLDER_DEPTH) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "收藏夹嵌套层级不能超过" + MAX_FOLDER_DEPTH + "层");
            }
            UserFolder parent = userFolderMapper.selectOne(new LambdaQueryWrapper<UserFolder>()
                    .eq(UserFolder::getId, currentParentId)
                    .eq(UserFolder::getUserId, userId)
                    .eq(UserFolder::getDeleted, NOT_DELETED)
                    .select(UserFolder::getParentId)
                    .last("limit 1"));
            if (parent == null) {
                break;
            }
            currentParentId = parent.getParentId();
        }
    }

    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
