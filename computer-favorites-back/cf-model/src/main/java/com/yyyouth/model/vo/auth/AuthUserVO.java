package com.yyyouth.model.vo.auth;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 登录用户基础信息
 */
@Data
public class AuthUserVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;
}
