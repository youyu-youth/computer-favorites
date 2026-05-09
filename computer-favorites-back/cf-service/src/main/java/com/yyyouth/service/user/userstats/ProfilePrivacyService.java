package com.yyyouth.service.user.userstats;

import com.yyyouth.model.dto.user.ProfilePrivacyUpdateDTO;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页隐私设置服务（user-15 M5）。
 *
 * 仅维护 t_user_setting 中 3 个字段：profile_visibility / show_contribution / show_collections。
 * 与 user-14 偏好设置（theme/language/page_size 等）解耦，避免互相覆盖。
 *
 * 写入完成后必须按 spec §6.4 失效 user:profile:public:{username} 缓存，否则旧开关会被缓存穿透。
 */
public interface ProfilePrivacyService {

    /**
     * 更新当前登录用户的主页隐私设置。
     *
     * @param userId 当前登录用户 ID
     * @param dto    隐私字段（已 @Valid 校验）
     */
    void updatePrivacy(Long userId, ProfilePrivacyUpdateDTO dto);
}
