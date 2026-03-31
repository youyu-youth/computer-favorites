package com.yyyouth.service.admin.auth;

import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员认证服务接口
 */
public interface AdminAuthenticationService {

    /**
     * 管理员登录
     *
     * @param loginDTO 登录参数
     * @param loginIp 登录IP
     * @return 登录结果
     */
    AuthLoginVO login(AuthLoginDTO loginDTO, String loginIp);
}
