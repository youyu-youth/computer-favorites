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
 * 用户会话实体
 */
@Data
@TableName("tb_user_session")
public class UserSession {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话Token
     */
    private String tokenValue;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 会话状态 1有效 0失效
     */
    private Integer status;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 最近续期时间
     */
    private LocalDateTime renewTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 操作来源
     */
    private String operationSource;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
