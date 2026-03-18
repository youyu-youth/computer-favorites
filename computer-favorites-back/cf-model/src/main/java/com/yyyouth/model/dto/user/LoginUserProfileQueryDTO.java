package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 登录用户资料查询参数
 */
@Data
public class LoginUserProfileQueryDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
