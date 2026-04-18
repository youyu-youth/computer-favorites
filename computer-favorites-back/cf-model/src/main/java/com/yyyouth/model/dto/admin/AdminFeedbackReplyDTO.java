package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈回复参数
 */
@Data
public class AdminFeedbackReplyDTO {

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(min = 10, max = 500, message = "回复内容长度需在10到500个字符之间")
    private String reply;
}
