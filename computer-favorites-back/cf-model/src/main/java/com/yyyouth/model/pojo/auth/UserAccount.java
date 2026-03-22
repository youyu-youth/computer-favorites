package com.yyyouth.model.pojo.auth;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 用户账号实体
 */
@Data
@TableName("t_user")
public class UserAccount {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 密码
     */
    private String password;

    /**
     * 加密密码
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 状态 1启用 0禁用
     */
    private Integer status;

    /**
     * 邮箱是否验证 1已验证 0未验证
     */
    private Integer emailVerified;

    /**
     * 手机号是否验证 1已验证 0未验证
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

    /**
     * 用户名最后修改时间
     */
    @TableField(value = "username_update_time", select = false)
    private LocalDateTime usernameUpdateTime;

    /**
     * 逻辑删除标记 0未删除 1已删除
     */
    private Integer deleted;
}
