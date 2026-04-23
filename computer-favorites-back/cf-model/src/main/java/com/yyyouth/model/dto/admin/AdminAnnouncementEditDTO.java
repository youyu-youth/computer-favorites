package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-22
 *
 * 管理端编辑公告参数
 */
@Data
public class AdminAnnouncementEditDTO {

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题不能超过200个字符")
    private String title;

    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /**
     * 类型：1-新增内容，2-Bug修复，3-更新系统
     */
    @NotNull(message = "公告类型不能为空")
    @Min(value = 1, message = "类型值不合法")
    @Max(value = 3, message = "类型值不合法")
    private Integer type;

    /**
     * 是否置顶：0否，1是
     */
    @Min(value = 0, message = "置顶值不合法")
    @Max(value = 1, message = "置顶值不合法")
    private Integer isTop = 0;

    /**
     * 状态：0隐藏，1显示
     */
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status = 1;

    /**
     * 发布时间（可选）
     */
    private LocalDateTime publishTime;
}
