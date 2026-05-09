package com.yyyouth.model.vo.userstats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 公开主页 - 用户基础信息（脱敏，user-15 M5）。
 *
 * 不下发：email / phone / lastLoginIp / lastLoginTime / status / passwordHash
 */
@Data
@Builder
public class ProfilePublicUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID（公开值；前端可拼路由） */
    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    /** 加入日期（注册时间），脱敏到日级即可 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime joinDate;
}
