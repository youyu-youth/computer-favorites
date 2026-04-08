package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-02
 *
 * 管理端编辑网站参数
 */
@Data
public class AdminWebsiteEditDTO {

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
     * 网站图标
     */
    @Size(max = 500, message = "网站图标地址长度不能超过500")
    private String icon;

    /**
     * 网站简介
     */
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
     * 标签
     */
    @Size(max = 500, message = "标签长度不能超过500")
    private String tags;

    /**
     * 是否置顶
     */
    @NotNull(message = "置顶标识不能为空")
    private Boolean isTop = Boolean.FALSE;

    /**
     * 是否推荐
     */
    @NotNull(message = "推荐标识不能为空")
    private Boolean isRecommend = Boolean.FALSE;

    /**
     * 是否官方
     */
    private Boolean isOfficial;

    /**
     * 审核备注（仅待审核网站可更新）
     */
    @Size(max = 500, message = "审核备注长度不能超过500")
    private String auditRemark;

    /**
     * 排序值
     */
    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 999999, message = "排序值不能大于999999")
    private Integer sort = 0;
}
