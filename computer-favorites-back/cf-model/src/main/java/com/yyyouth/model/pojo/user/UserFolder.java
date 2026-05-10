package com.yyyouth.model.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏文件夹实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_folder")
public class UserFolder {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    @TableField("icon")
    private String icon;

    @TableField("color")
    private String color;

    @TableField("parent_id")
    private Long parentId;

    @TableField("sort")
    private Integer sort;

    @TableField("website_count")
    private Integer websiteCount;

    @TableField("is_hide")
    private Integer isHide;

    /**
     * 是否对其他用户可见：0-私密(默认)，1-公开
     */
    @TableField("is_public")
    private Integer isPublic;

    @TableField("is_default")
    private Integer isDefault;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;
}
