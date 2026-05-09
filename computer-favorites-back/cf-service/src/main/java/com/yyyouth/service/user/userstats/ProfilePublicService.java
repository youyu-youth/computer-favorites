package com.yyyouth.service.user.userstats;

import com.yyyouth.model.vo.userstats.ProfilePublicVO;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户公开主页查询服务（user-15 M5）。
 *
 * 仅暴露脱敏后的基础资料，并按 t_user_setting 中 3 个隐私开关进行权限过滤：
 *  - profile_visibility=public  ：所有人可见
 *  - profile_visibility=logged  ：未登录访问 → PROFILE_LOGIN_REQUIRED
 *  - profile_visibility=private ：非本人访问 → PROFILE_PRIVATE
 *
 * 看板 5 个 section（overview/graph/category/radar/trend）单独走 ProfileDashboardService，
 * 但其入口处需复用本服务的隐私校验。
 */
public interface ProfilePublicService {

    /**
     * 加载目标用户的公开主页。
     *
     * @param username      目标用户名
     * @param currentUserId 当前登录者 ID（未登录传 null）
     * @return 公开主页聚合 VO；不可见 / 用户不存在场景抛 BusinessException
     */
    ProfilePublicVO getPublicProfile(String username, Long currentUserId);

    /**
     * 校验访问者对目标用户的看板访问权限（不构造 VO，仅做权限断言）。
     * 用于公共看板接口入口，避免重复加载 setting。
     *
     * @param targetUserId   目标用户 ID
     * @param currentUserId  当前登录者 ID（未登录传 null）
     * @param requireContribution 是否需要"贡献"维度（看板的 overview/graph/trend 需要 true）
     * @return 目标用户当前的隐私快照（含 visibility / showContribution / showCollections）
     */
    ProfileVisibilityCheckResult checkDashboardAccess(Long targetUserId,
                                                     Long currentUserId,
                                                     boolean requireContribution);

    /**
     * 隐私校验返回值（仅服务层内部 / 被复用的公共看板入口使用）。
     */
    record ProfileVisibilityCheckResult(boolean isOwn,
                                        String profileVisibility,
                                        Integer showContribution,
                                        Integer showCollections) { }
}
