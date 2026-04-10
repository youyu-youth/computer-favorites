package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-09
 *
 * 管理端重置用户密码参数
 */
@Data
public class AdminUserPasswordResetDTO {

    /**
     * 新密码（6-32位）
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6到32位之间")
    private String newPassword;
}
