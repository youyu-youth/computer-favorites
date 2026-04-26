package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 文件夹下拉选项
 */
@Data
@Builder
public class UserFolderOptionsVO {

    private Long id;

    private String name;

    private String icon;

    private String color;
}
