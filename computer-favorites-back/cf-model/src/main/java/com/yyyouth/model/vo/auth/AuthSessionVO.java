package com.yyyouth.model.vo.auth;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 当前会话信息
 */
@Data
public class AuthSessionVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * Token值
     */
    private String tokenValue;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * Token剩余有效秒数
     */
    private long timeoutSeconds;

    /**
     * Token过期时间
     */
    private LocalDateTime expireTime;
}
