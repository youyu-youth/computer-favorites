package com.yyyouth.service.user.userstats.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.enums.ProfileVisibility;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.userstats.ProfilePublicPrivacyVO;
import com.yyyouth.model.vo.userstats.ProfilePublicProfileVO;
import com.yyyouth.model.vo.userstats.ProfilePublicUserVO;
import com.yyyouth.model.vo.userstats.ProfilePublicVO;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.redis.RedisCache;
import com.yyyouth.service.user.userstats.ProfilePublicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 公开主页查询实现（user-15 M5）。
 *
 * 关键策略：
 *  1. 用户存在性校验先于隐私校验，但任意失败都用同一错误码（USER_DISABLED 防枚举）；
 *  2. visibility=PRIVATE 且非本人 → 不下发任何字段；visibility=LOGGED 且未登录 → 401 等价；
 *  3. 缓存 user:profile:public:{username}（TTL 5min ±60s），仅缓存"目标侧"的快照，
 *     isOwn 由调用侧实时计算补齐，确保不同访问者拿到正确视图；
 *  4. UserSetting 在新用户场景可能尚未生成行：本服务读取空时按 PUBLIC + 1 + 1 兜底（最宽松，
 *     与 spec §3.3 默认值一致），不报错。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilePublicServiceImpl implements ProfilePublicService {

    /** 公开主页缓存 TTL：5min；spec §6.4 */
    private static final long PUBLIC_CACHE_TTL_SECONDS = 300L;

    /** 抖动 ±60s */
    private static final long PUBLIC_CACHE_JITTER_SECONDS = 60L;

    /** 默认隐私（所有字段缺失时兜底） */
    private static final String DEFAULT_VISIBILITY = ProfileVisibility.PUBLIC.getCode();
    private static final int DEFAULT_SHOW_CONTRIBUTION = 1;
    private static final int DEFAULT_SHOW_COLLECTIONS = 1;

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserSettingMapper userSettingMapper;
    private final RedisCache redisCache;

    @Override
    public ProfilePublicVO getPublicProfile(String username, Long currentUserId) {
        if (!StringUtils.hasText(username)) {
            throw userNotFound();
        }
        String normalized = username.trim();

        String key = RedisConstant.USER_PROFILE_PUBLIC_PREFIX + normalized;
        ProfilePublicVO snapshot = redisCache.getOrLoad(key, ProfilePublicVO.class,
                PUBLIC_CACHE_TTL_SECONDS, PUBLIC_CACHE_JITTER_SECONDS,
                () -> loadPublicProfileFromDb(normalized));

        // 隐私校验在 snapshot 之外执行：缓存可能被多个访问者共享
        applyVisibilityGuard(snapshot, currentUserId);
        // 补 isOwn（缓存里始终为 false）
        boolean isOwn = currentUserId != null && snapshot.getUser() != null
                && currentUserId.equals(snapshot.getUser().getId());
        snapshot.setIsOwn(isOwn);
        return snapshot;
    }

    @Override
    public ProfileVisibilityCheckResult checkDashboardAccess(Long targetUserId,
                                                             Long currentUserId,
                                                             boolean requireContribution) {
        if (targetUserId == null) {
            throw userNotFound();
        }
        UserAccount account = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, targetUserId)
                .eq(UserAccount::getDeleted, 0)
                .last("limit 1"));
        if (account == null) {
            throw userNotFound();
        }
        UserSetting setting = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, targetUserId)
                .last("limit 1"));
        String visibility = setting != null && StringUtils.hasText(setting.getProfileVisibility())
                ? setting.getProfileVisibility() : DEFAULT_VISIBILITY;
        Integer showContribution = setting != null && setting.getShowContribution() != null
                ? setting.getShowContribution() : DEFAULT_SHOW_CONTRIBUTION;
        Integer showCollections = setting != null && setting.getShowCollections() != null
                ? setting.getShowCollections() : DEFAULT_SHOW_COLLECTIONS;

        boolean isOwn = currentUserId != null && currentUserId.equals(targetUserId);
        guardVisibility(visibility, isOwn, currentUserId != null);
        if (!isOwn && requireContribution && showContribution != null && showContribution == 0) {
            // 贡献相关板块被屏蔽时，对外按 PRIVATE 处理（避免空数据被误读为"无活动"）
            throw new BusinessException(AuthErrorCode.PROFILE_PRIVATE.getCode(),
                    AuthErrorCode.PROFILE_PRIVATE.getMessage());
        }
        return new ProfileVisibilityCheckResult(isOwn, visibility, showContribution, showCollections);
    }

    // ===================== private =====================

    private ProfilePublicVO loadPublicProfileFromDb(String username) {
        UserAccount account = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, username)
                .eq(UserAccount::getDeleted, 0)
                .last("limit 1"));
        if (account == null) {
            // 与 visibility=PRIVATE 错误一致防枚举：直接抛"不存在"——这里返回 null 让缓存空值兜底也可，
            // 但 USER_DISABLED 与 PROFILE_PRIVATE 信息维度不同，前端会做不同空态。这里先抛错。
            throw userNotFound();
        }
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, account.getId())
                .eq(UserProfile::getDeleted, 0)
                .last("limit 1"));
        UserSetting setting = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, account.getId())
                .last("limit 1"));

        String visibility = setting != null && StringUtils.hasText(setting.getProfileVisibility())
                ? setting.getProfileVisibility() : DEFAULT_VISIBILITY;
        Integer showContribution = setting != null && setting.getShowContribution() != null
                ? setting.getShowContribution() : DEFAULT_SHOW_CONTRIBUTION;
        Integer showCollections = setting != null && setting.getShowCollections() != null
                ? setting.getShowCollections() : DEFAULT_SHOW_COLLECTIONS;

        ProfilePublicUserVO userVO = ProfilePublicUserVO.builder()
                .id(account.getId())
                .username(account.getUsername())
                .nickname(account.getNickname())
                .avatar(account.getAvatar())
                .joinDate(account.getCreateTime())
                .build();

        ProfilePublicProfileVO profileVO = ProfilePublicProfileVO.builder()
                .signature(profile != null ? profile.getSignature() : null)
                .country(profile != null ? profile.getCountry() : null)
                .city(profile != null ? profile.getCity() : null)
                .githubUrl(profile != null ? profile.getGithubUrl() : null)
                .giteeUrl(profile != null ? profile.getGiteeUrl() : null)
                .blogUrl(profile != null ? profile.getBlogUrl() : null)
                .hobbyTags(profile != null ? profile.getHobbyTags() : null)
                .techStack(profile != null ? profile.getTechStack() : null)
                .build();

        ProfilePublicPrivacyVO privacyVO = ProfilePublicPrivacyVO.builder()
                .profileVisibility(visibility)
                .showContribution(showContribution)
                .showCollections(showCollections)
                .build();

        return ProfilePublicVO.builder()
                .user(userVO)
                .profile(profileVO)
                .privacy(privacyVO)
                .isOwn(false)
                .build();
    }

    /**
     * 在缓存命中后做隐私校验。snapshot 永不为 null（loadPublicProfileFromDb 在用户不存在时已抛错）。
     */
    private void applyVisibilityGuard(ProfilePublicVO snapshot, Long currentUserId) {
        if (snapshot == null || snapshot.getUser() == null || snapshot.getPrivacy() == null) {
            throw userNotFound();
        }
        boolean isOwn = currentUserId != null
                && currentUserId.equals(snapshot.getUser().getId());
        guardVisibility(snapshot.getPrivacy().getProfileVisibility(), isOwn, currentUserId != null);
    }

    /**
     * visibility 通用守卫。
     * 注意 spec §4.4：private 对所有非本人返回相同错误，避免暴露用户存在性。
     */
    private void guardVisibility(String visibilityCode, boolean isOwn, boolean isLogged) {
        if (isOwn) {
            return;
        }
        ProfileVisibility v = ProfileVisibility.fromCode(visibilityCode);
        if (v == ProfileVisibility.PRIVATE) {
            throw new BusinessException(AuthErrorCode.PROFILE_PRIVATE.getCode(),
                    AuthErrorCode.PROFILE_PRIVATE.getMessage());
        }
        if (v == ProfileVisibility.LOGGED && !isLogged) {
            throw new BusinessException(AuthErrorCode.PROFILE_LOGIN_REQUIRED.getCode(),
                    AuthErrorCode.PROFILE_LOGIN_REQUIRED.getMessage());
        }
    }

    /**
     * 用户不存在 / 已禁用：复用 USER_DISABLED 错误码（语义"不存在或已被禁用"），
     * 与 spec §4.4 防枚举要求一致。
     */
    private BusinessException userNotFound() {
        return new BusinessException(AuthErrorCode.USER_DISABLED.getCode(),
                AuthErrorCode.USER_DISABLED.getMessage());
    }
}
