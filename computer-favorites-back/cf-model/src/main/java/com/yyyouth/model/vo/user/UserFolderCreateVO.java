package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 创建收藏文件夹返回结果
 */
@Data
@Builder
public class UserFolderCreateVO {

    private Long id;

    private String name;

    private String icon;

    private String color;

    private Long parentId;

    private Integer sort;
}
