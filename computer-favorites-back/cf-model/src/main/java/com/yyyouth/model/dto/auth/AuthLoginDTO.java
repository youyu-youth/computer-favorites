package com.yyyouth.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 登录请求参数
 */
@Data
public class AuthLoginDTO {

    /**
     * 登录账号
     */
    @NotBlank(message = "账号不能为空")
    private String username;

    /**
     * 登录密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 设备类型
     */
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;
}
