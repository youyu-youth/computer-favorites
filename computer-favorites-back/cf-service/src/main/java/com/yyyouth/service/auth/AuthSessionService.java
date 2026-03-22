package com.yyyouth.service.auth;

import com.yyyouth.model.vo.auth.AuthSessionVO;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * 认证会话服务接口
 */
public interface AuthSessionService {

    /**
     * 会话续期
     */
    void renewSession();

    /**
     * 退出登录
     */
    void logout();

    /**
     * 查询当前会话
     *
     * @return 会话信息
     */
    AuthSessionVO currentSession();
}
