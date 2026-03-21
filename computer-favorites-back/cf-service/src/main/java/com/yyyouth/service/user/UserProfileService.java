package com.yyyouth.service.user;

import com.yyyouth.model.vo.user.LoginUserProfileVO;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 登录用户资料服务接口
 */
public interface UserProfileService {

    /**
     * 查询登录用户资料
     *
     * @return 登录用户资料
     */
    LoginUserProfileVO queryLoginUserProfile();
}
