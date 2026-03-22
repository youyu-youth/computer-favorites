package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * 用户名修改参数
 */
@Data
public class UserUsernameUpdateDTO {

    /**
     * 新用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 120, message = "用户名长度需在1-120之间")
    private String username;
}
