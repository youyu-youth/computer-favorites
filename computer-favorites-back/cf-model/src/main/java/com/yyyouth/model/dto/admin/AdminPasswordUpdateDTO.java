package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员修改密码参数
 */
@Data
public class AdminPasswordUpdateDTO {

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
}