package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 密码验证参数
 */
@Data
public class PasswordVerifyDTO {

    @NotBlank(message = "密码不能为空")
    private String password;
}
