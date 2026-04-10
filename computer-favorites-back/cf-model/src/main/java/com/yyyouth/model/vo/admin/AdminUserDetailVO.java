package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-09
 *
 * 管理端用户详情（聚合基础信息+资料）
 */
@Data
public class AdminUserDetailVO {

    // ---- 基础信息（t_user） ----

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态：0禁用，1正常
     */
    private Integer status;

    /**
     * 邮箱验证：0未验证，1已验证
     */
    private Integer emailVerified;

    /**
     * 手机验证：0未验证，1已验证
     */
    private Integer phoneVerified;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ---- 用户资料（t_user_profile） ----

    /**
     * 性别：0未知，1男，2女，3保密
     */
    private Integer gender;

    /**
     * 城市
     */
    private String city;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 技术栈（逗号分隔ID列表）
     */
    private String techStack;

    /**
     * GitHub地址
     */
    private String githubUrl;

    /**
     * 博客地址
     */
    private String blogUrl;
}
