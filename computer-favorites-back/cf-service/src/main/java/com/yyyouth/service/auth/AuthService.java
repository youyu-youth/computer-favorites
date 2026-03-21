package com.yyyouth.service.auth;

import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.dto.auth.AuthLoginEmailCodeDTO;
import com.yyyouth.model.dto.auth.AuthRegisterDTO;
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
     * 邮箱验证码登录
     *
     * @param loginEmailCodeDTO 登录参数
     * @return 登录结果
     */
    AuthLoginVO loginByEmailCode(AuthLoginEmailCodeDTO loginEmailCodeDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     */
    void register(AuthRegisterDTO registerDTO);

    /**
     * 发送注册邮箱验证码
     *
     * @param email 注册邮箱
     */
    void sendRegisterEmailCode(String email);

    /**
     * 发送登录邮箱验证码
     *
     * @param email 登录邮箱
     */
    void sendLoginEmailCode(String email);

    /**
     * 校验用户名是否可用
     *
     * @param username 用户名
     * @return true可用 false不可用
     */
    boolean checkUsernameAvailable(String username);

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
