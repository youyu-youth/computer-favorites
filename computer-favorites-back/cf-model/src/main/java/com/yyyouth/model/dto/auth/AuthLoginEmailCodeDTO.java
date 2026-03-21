package com.yyyouth.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-20
 *
 * 邮箱验证码登录参数
 */
@Data
public class AuthLoginEmailCodeDTO {

    /**
     * 登录邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128")
    private String email;

    /**
     * 邮箱验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码长度必须为6位")
    private String emailCode;

    /**
     * 设备类型
     */
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;
}
