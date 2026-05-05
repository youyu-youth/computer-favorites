package com.yyyouth.model.pojo.website;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 评论举报实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_comment_report")
public class CommentReport {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 举报用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 被举报的评论ID
     */
    @TableField("comment_id")
    private Long commentId;

    /**
     * 被举报评论的作者ID
     */
    @TableField("comment_user_id")
    private Long commentUserId;

    /**
     * 举报原因类型：1垃圾广告 2侮辱谩骂 3虚假信息 4违法违规 5其他
     */
    @TableField("reason_type")
    private Integer reasonType;

    /**
     * 举报详细说明
     */
    private String reason;

    /**
     * 截图证据（JSON数组）
     */
    private String images;

    /**
     * 处理状态：0待处理 1已处理 2已驳回
     */
    private Integer status;

    /**
     * 处理结果说明
     */
    @TableField("handle_result")
    private String handleResult;

    /**
     * 处理管理员ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

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
