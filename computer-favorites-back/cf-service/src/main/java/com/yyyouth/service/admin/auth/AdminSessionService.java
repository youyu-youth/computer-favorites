package com.yyyouth.service.admin.auth;

import com.yyyouth.model.vo.auth.AuthSessionVO;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员会话服务接口
 */
public interface AdminSessionService {

    /**
     * 管理员会话续期
     */
    void renewSession();

    /**
     * 管理员退出登录
     */
    void logout();

    /**
     * 查询管理员当前会话
     *
     * @return 当前会话
     */
    AuthSessionVO currentSession();
}
