package com.yyyouth.model.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户设置实体
 */
@Data
@TableName("t_user_setting")
public class UserSetting {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 主题
     */
    @TableField("theme")
    private String theme;

    /**
     * 语言
     */
    @TableField("language")
    private String language;

    /**
     * 邮件通知
     */
    @TableField("email_notice")
    private Integer emailNotice;

    /**
     * 收藏通知
     */
    @TableField("collect_notice")
    private Integer collectNotice;

    /**
     * 评论通知
     */
    @TableField("comment_notice")
    private Integer commentNotice;

    /**
     * 主页样式
     */
    @TableField("homepage_style")
    private String homepageStyle;

    /**
     * 每页条数
     */
    @TableField("page_size")
    private Integer pageSize;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
