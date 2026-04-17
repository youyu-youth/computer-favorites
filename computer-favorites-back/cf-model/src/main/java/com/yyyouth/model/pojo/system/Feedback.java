package com.yyyouth.model.pojo.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈实体
 */
@Data
@Builder
@TableName("t_feedback")
public class Feedback {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 反馈用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 反馈类型
     */
    private Integer type;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 附图地址 JSON 数组
     */
    private String images;

    /**
     * 处理状态
     */
    private Integer status;

    /**
     * 回复内容
     */
    private String reply;

    /**
     * 回复时间
     */
    @TableField("reply_time")
    private LocalDateTime replyTime;

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
