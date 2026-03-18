package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 登录用户资料聚合视图
 */
@Data
public class LoginUserProfileVO {

    /**
     * 用户基础信息
     */
    private UserBasicInfoVO user;

    /**
     * 用户资料信息
     */
    private UserDetailProfileVO profile;

    /**
     * 用户设置信息
     */
    private UserPreferenceSettingVO setting;
}
