package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端单条举报处置参数
 */
@Data
public class AdminReportHandleDTO {

    /**
     * 处置动作：pass 通过，reject 驳回
     */
    @NotBlank(message = "处置动作不能为空")
    @Pattern(regexp = "pass|reject", message = "处置动作不合法")
    private String action;

    /**
     * 处理说明
     */
    @NotBlank(message = "处理说明不能为空")
    @Size(min = 10, max = 500, message = "处理说明长度需在10到500个字符之间")
    private String handleResult;

    /**
     * 是否执行联动动作
     */
    @NotNull(message = "是否执行联动动作不能为空")
    private Boolean executeAction = Boolean.TRUE;
}
