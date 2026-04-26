package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 更新收藏文件夹参数
 */
@Data
public class UserFolderUpdateDTO {

    @Size(max = 50, message = "文件夹名称不能超过50个字符")
    private String name;

    @Size(max = 100, message = "文件夹图标不能超过100个字符")
    private String icon;

    private String color;

    private Integer sort;
}
