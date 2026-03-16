package com.yyyouth.model.pojo.auth;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("cf_user_account")
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
     * 加密密码
     */
    private String passwordHash;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 状态 1启用 0禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
