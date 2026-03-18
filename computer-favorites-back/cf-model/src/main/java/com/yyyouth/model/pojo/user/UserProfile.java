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
 * 用户资料实体
 */
@Data
@TableName("t_user_profile")
public class UserProfile {

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
     * 性别
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * GitHub 地址
     */
    @TableField("github_url")
    private String githubUrl;

    /**
     * Gitee 地址
     */
    @TableField("gitee_url")
    private String giteeUrl;

    /**
     * 其他代码仓链接
     */
    @TableField("other_repo_links")
    private String otherRepoLinks;

    /**
     * 博客地址
     */
    @TableField("blog_url")
    private String blogUrl;

    /**
     * 个性签名
     */
    @TableField("signature")
    private String signature;

    /**
     * 兴趣标签
     */
    @TableField("hobby_tags")
    private String hobbyTags;

    /**
     * 技术栈
     */
    @TableField("tech_stack")
    private String techStack;

    /**
     * 收藏网站
     */
    @TableField("favorite_websites")
    private String favoriteWebsites;

    /**
     * 上传网站
     */
    @TableField("uploaded_websites")
    private String uploadedWebsites;

    /**
     * 贡献值
     */
    @TableField("contribution")
    private String contribution;

    /**
     * 逻辑删除 0未删除 1已删除
     */
    @TableField("deleted")
    private Integer deleted;

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
