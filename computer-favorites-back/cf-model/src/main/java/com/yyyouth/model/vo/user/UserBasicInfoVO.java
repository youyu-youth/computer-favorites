package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户基础信息视图
 */
@Data
public class UserBasicInfoVO {

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
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱是否验证
     */
    private Integer emailVerified;

    /**
     * 手机号是否验证
     */
    private Integer phoneVerified;
}
