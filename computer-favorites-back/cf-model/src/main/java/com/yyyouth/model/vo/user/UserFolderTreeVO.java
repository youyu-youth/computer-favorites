package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏文件夹树节点
 */
@Data
@Builder
public class UserFolderTreeVO {

    private Long id;

    private String name;

    private String icon;

    private String color;

    private Long parentId;

    private Integer sort;

    private Integer websiteCount;

    private Integer isHide;

    /** 是否对外公开：0-私密(默认)，1-公开 */
    private Integer isPublic;

    private Integer isDefault;

    private List<UserFolderTreeVO> children;
}
