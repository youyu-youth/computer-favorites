package com.yyyouth.model.vo.userstats;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 公开主页聚合视图（user-15 M5）。
 *
 * 对应 GET /api/user/profile/public/{username}。
 *
 * 字段组合策略：
 *  - {@link #user}：脱敏后的基础账号信息
 *  - {@link #profile}：脱敏后的个人资料
 *  - {@link #privacy}：3 个开关元信息（前端按此渲染空态）
 *  - {@link #isOwn}：当前调用者是否为本人（true 时前端展示编辑入口、忽略 show_* 开关）
 *
 * 当目标用户 visibility=PRIVATE 且非本人 / visibility=LOGGED 且未登录时，
 * 服务层不会构造此 VO，而是抛 BusinessException(PROFILE_PRIVATE / PROFILE_LOGIN_REQUIRED)。
 */
@Data
@Builder
public class ProfilePublicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private ProfilePublicUserVO user;

    private ProfilePublicProfileVO profile;

    private ProfilePublicPrivacyVO privacy;

    /** 当前查看者是否就是该主页的所有者 */
    private Boolean isOwn;
}
