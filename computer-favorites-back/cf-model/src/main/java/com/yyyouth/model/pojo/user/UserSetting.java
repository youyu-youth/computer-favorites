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
     * 收藏夹访问密码
     */
    @TableField("favorites_hide_password")
    private String favoritesHidePassword;

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
     * 主页可见性 public=所有人 / logged=仅登录用户 / private=仅本人
     */
    @TableField("profile_visibility")
    private String profileVisibility;

    /**
     * 是否对他人展示贡献热力图与贡献分 1-是 0-否
     */
    @TableField("show_contribution")
    private Integer showContribution;

    /**
     * 是否对他人展示收藏列表 1-是 0-否
     */
    @TableField("show_collections")
    private Integer showCollections;

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
