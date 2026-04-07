package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿网站参数
 */
@Data
public class UserWebsiteSubmissionCreateDTO {

    /**
     * 网站名称
     */
    @NotBlank(message = "网站名称不能为空")
    @Size(max = 100, message = "网站名称长度不能超过100")
    private String name;

    /**
     * 网站URL
     */
    @NotBlank(message = "网站URL不能为空")
    @Size(max = 500, message = "网站URL长度不能超过500")
    @Pattern(regexp = "^https?://.+$", message = "网站URL格式不正确")
    private String url;

    /**
     * 网站图标地址
     */
    @NotBlank(message = "网站图标地址不能为空")
    @Size(max = 500, message = "网站图标地址长度不能超过500")
    @Pattern(regexp = "^https?://.+$", message = "网站图标地址格式不正确")
    private String icon;

    /**
     * Github 地址
     */
    @Size(max = 255, message = "Github地址长度不能超过255")
    @Pattern(regexp = "^$|^https?://.+$", message = "Github地址格式不正确")
    private String githubUrl;

    /**
     * 网站简介
     */
    @NotBlank(message = "网站简介不能为空")
    @Size(max = 200, message = "网站简介长度不能超过200")
    private String summary;

    /**
     * 网站描述
     */
    @Size(max = 10000, message = "网站描述长度不能超过10000")
    private String description;

    /**
     * 分类ID
     */
    @NotNull(message = "分类不能为空")
    @Min(value = 1, message = "分类参数不合法")
    private Long categoryId;

    /**
     * 标签ID列表（逗号分隔）
     */
    @Size(max = 500, message = "标签长度不能超过500")
    private String tags;
}
