package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 创建收藏文件夹参数
 */
@Data
public class UserFolderCreateDTO {

    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 50, message = "文件夹名称不能超过50个字符")
    private String name;

    @Size(max = 100, message = "文件夹图标不能超过100个字符")
    private String icon;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "文件夹颜色格式不合法，须为HEX格式，例如#f59e0b")
    @Size(max = 20, message = "文件夹颜色不能超过20个字符")
    private String color;

    @NotNull(message = "父文件夹ID不能为空")
    @Min(value = 0, message = "父文件夹ID不合法")
    private Long parentId;

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能为负数")
    @Max(value = 9999, message = "排序值不能超过9999")
    private Integer sort;
}
