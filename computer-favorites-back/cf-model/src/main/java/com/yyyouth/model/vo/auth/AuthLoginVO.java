package com.yyyouth.model.vo.auth;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 登录响应参数
 */
@Data
public class AuthLoginVO {

    /**
     * Token值
     */
    private String tokenValue;

    /**
     * Token名称
     */
    private String tokenName;

    /**
     * Token过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 登录用户信息
     */
    private AuthUserVO userInfo;
}
