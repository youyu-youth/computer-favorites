package com.yyyouth.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户详情资料视图
 */
@Data
public class UserDetailProfileVO {

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 国家
     */
    private String country;

    /**
     * 城市
     */
    private String city;

    /**
     * GitHub 地址
     */
    private String githubUrl;

    /**
     * Gitee 地址
     */
    private String giteeUrl;

    /**
     * 其他代码仓链接
     */
    private String otherRepoLinks;

    /**
     * 博客地址
     */
    private String blogUrl;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 兴趣标签
     */
    private String hobbyTags;

    /**
     * 技术栈
     */
    private String techStack;

    /**
     * 收藏网站
     */
    private String favoriteWebsites;

    /**
     * 上传网站
     */
    private String uploadedWebsites;

    /**
     * 贡献值
     */
    private String contribution;

    /**
     * 逻辑删除标记
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
