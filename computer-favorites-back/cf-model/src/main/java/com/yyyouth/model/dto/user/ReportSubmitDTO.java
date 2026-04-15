package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 举报提交请求参数
 */
@Data
public class ReportSubmitDTO {

    /**
     * 举报类型：1=网站，2=评论
     */
    @NotNull(message = "举报类型不能为空")
    @Min(value = 1, message = "举报类型必须为1或2")
    @Max(value = 2, message = "举报类型必须为1或2")
    private Integer type;

    /**
     * 举报目标ID
     */
    @NotNull(message = "举报目标ID不能为空")
    @Min(value = 1, message = "举报目标ID必须大于0")
    private Long targetId;

    /**
     * 举报原因
     */
    @NotBlank(message = "举报原因不能为空")
    @Size(min = 10, max = 500, message = "举报原因长度需在10-500字符之间")
    private String reason;

    /**
     * 截图证据URL数组（最多5个）
     */
    @Size(max = 5, message = "最多上传5张截图")
    private List<String> images;
}
