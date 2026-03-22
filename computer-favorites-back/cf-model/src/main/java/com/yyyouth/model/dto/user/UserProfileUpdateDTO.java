package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * 用户资料更新参数
 */
@Data
public class UserProfileUpdateDTO {

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    /**
     * 性别：0未知 1男 2女 3保密
     */
    @Min(value = 0, message = "性别参数不合法")
    @Max(value = 3, message = "性别参数不合法")
    private Integer gender;

    /**
     * 国家
     */
    @Size(max = 100, message = "国家长度不能超过100")
    private String country;

    /**
     * 城市
     */
    @Size(max = 100, message = "城市长度不能超过100")
    private String city;

    /**
     * GitHub 地址
     */
    @Size(max = 500, message = "GitHub地址长度不能超过500")
    private String githubUrl;

    /**
     * Gitee 地址
     */
    @Size(max = 500, message = "Gitee地址长度不能超过500")
    private String giteeUrl;

    /**
     * 其他代码仓链接
     */
    @Size(max = 2000, message = "其他代码仓链接长度不能超过2000")
    private String otherRepoLinks;

    /**
     * 博客地址
     */
    @Size(max = 500, message = "博客地址长度不能超过500")
    private String blogUrl;

    /**
     * 个性签名
     */
    @Size(max = 500, message = "个性签名长度不能超过500")
    private String signature;

    /**
     * 兴趣标签，逗号分隔
     */
    @Size(max = 1000, message = "兴趣标签长度不能超过1000")
    private String hobbyTags;

    /**
     * 技术栈，支持ID串或名称串
     */
    @Size(max = 1000, message = "技术栈长度不能超过1000")
    private String techStack;
}
