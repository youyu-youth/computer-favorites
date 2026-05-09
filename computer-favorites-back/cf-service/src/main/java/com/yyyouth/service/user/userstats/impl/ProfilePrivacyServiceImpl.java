package com.yyyouth.service.user.userstats.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.ProfilePrivacyUpdateDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.redis.RedisCache;
import com.yyyouth.service.user.userstats.ProfilePrivacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 隐私设置实现（user-15 M5）。
 *
 * 关键策略：
 *  1. 仅写 3 个隐私字段，不触碰偏好设置（避免与 user-14 互相覆盖）；
 *  2. 用户首次设置（t_user_setting 无行）时插入新行并填充默认偏好字段空值；
 *  3. 写入成功后立即失效 user:profile:public:{username} + user:profile:dashboard:{userId}:*，
 *     避免他人继续读到旧开关。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilePrivacyServiceImpl implements ProfilePrivacyService {

    private final UserSettingMapper userSettingMapper;
    private final UserAccountMapper userAccountMapper;
    private final RedisCache redisCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivacy(Long userId, ProfilePrivacyUpdateDTO dto) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(AuthErrorCode.UNAUTHORIZED.getCode(),
                    AuthErrorCode.UNAUTHORIZED.getMessage());
        }
        if (dto == null) {
            throw new BusinessException(AuthErrorCode.USER_SETTING_INVALID_PARAM.getCode(),
                    AuthErrorCode.USER_SETTING_INVALID_PARAM.getMessage());
        }

        UserAccount account = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .select(UserAccount::getId, UserAccount::getUsername)
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getDeleted, 0)
                .last("limit 1"));
        if (account == null) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(),
                    AuthErrorCode.USER_DISABLED.getMessage());
        }

        UserSetting setting = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, userId)
                .last("limit 1"));

        boolean shouldInsert = setting == null;
        if (shouldInsert) {
            setting = new UserSetting();
            setting.setUserId(userId);
            setting.setCreateTime(LocalDateTime.now());
        }
        setting.setProfileVisibility(dto.getProfileVisibility());
        setting.setShowContribution(dto.getShowContribution());
        setting.setShowCollections(dto.getShowCollections());
        setting.setUpdateTime(LocalDateTime.now());

        int affected = shouldInsert
                ? userSettingMapper.insert(setting)
                : userSettingMapper.updateById(setting);
        if (affected != 1) {
            throw new BusinessException(AuthErrorCode.PROFILE_PRIVACY_UPDATE_FAILED.getCode(),
                    AuthErrorCode.PROFILE_PRIVACY_UPDATE_FAILED.getMessage());
        }

        evictPublicCache(account.getUsername(), userId);
        log.info("[privacy] update userId={}, visibility={}, showContribution={}, showCollections={}",
                userId, dto.getProfileVisibility(), dto.getShowContribution(), dto.getShowCollections());
    }

    /**
     * 失效公开主页 + 看板缓存（看板侧因隐私字段变更而需要重新生成空态字段）。
     * 失败仅记录日志：缓存失效非主流程，TTL 过期后会自然回源。
     */
    private void evictPublicCache(String username, Long userId) {
        try {
            if (StringUtils.hasText(username)) {
                redisCache.evict(RedisConstant.USER_PROFILE_PUBLIC_PREFIX + username);
            }
            redisCache.evictByPattern(RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId + ":*");
        } catch (Exception ex) {
            log.warn("[privacy] evict cache fail userId={}, username={}: {}",
                    userId, username, ex.getMessage());
        }
    }
}
