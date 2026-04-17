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
 * @date 2026-04-17
 *
 * 用户反馈提交参数
 */
@Data
public class UserFeedbackCreateDTO {

    /**
     * 反馈类型：1建议，2Bug反馈，3投诉，4使用感受
     */
    @NotNull(message = "反馈类型不能为空")
    @Min(value = 1, message = "反馈类型不合法")
    @Max(value = 4, message = "反馈类型不合法")
    private Integer type;

    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 10, max = 500, message = "反馈内容长度需在10-500字符之间")
    private String content;

    /**
     * 联系方式
     */
    @Size(max = 100, message = "联系方式长度不能超过100字符")
    private String contact;

    /**
     * 反馈附图 URL
     */
    @Size(max = 5, message = "最多上传5张图片")
    private List<@NotBlank(message = "图片地址不能为空") String> images;
}
