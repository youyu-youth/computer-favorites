package com.yyyouth.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * 修改密码请求参数
 */
@Data
public class AuthPasswordChangeDTO {

    /**
     * 当前密码
     */
    @NotBlank(message = "当前密码不能为空")
    @Size(min = 6, max = 64, message = "当前密码长度需在6-64之间")
    private String currentPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 64, message = "新密码长度需在8-64之间")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
            message = "新密码必须包含字母、数字和特殊字符"
    )
    private String newPassword;

    /**
     * 确认新密码
     */
    @NotBlank(message = "确认新密码不能为空")
    @Size(min = 8, max = 64, message = "确认新密码长度需在8-64之间")
    private String confirmPassword;

    /**
     * 账号绑定邮箱
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
}
