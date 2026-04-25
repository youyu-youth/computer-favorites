package com.yyyouth.service.user.folder.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserFolderCreateDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.UserFolder;
import com.yyyouth.model.vo.user.UserFolderCreateVO;
import com.yyyouth.service.mapper.user.UserFolderMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.user.folder.UserFolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

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

    private final UserFolderMapper userFolderMapper;
    private final UserAccountMapper userAccountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserFolderCreateVO createFolder(UserFolderCreateDTO createDTO) {
        UserAccount currentUser = getCurrentActiveUser();
        Long userId = currentUser.getId();

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

        return UserFolderCreateVO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .icon(folder.getIcon())
                .color(folder.getColor())
                .parentId(folder.getParentId())
                .sort(folder.getSort())
                .build();
    }

    private UserAccount getCurrentActiveUser() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getStatus, ENABLED_STATUS)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户未登录或账号不可用");
        }
        return userAccount;
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
