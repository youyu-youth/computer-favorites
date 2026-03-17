package com.yyyouth.service.auth.impl;

import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    AuthLoginVO login(AuthLoginDTO loginDTO);

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
