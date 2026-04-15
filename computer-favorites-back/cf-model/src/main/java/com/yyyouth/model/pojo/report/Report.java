package com.yyyouth.model.pojo.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 举报实体
 */
@Data
@Builder
@TableName("t_report")
public class Report {

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
     * 举报类型：1=网站，2=评论
     */
    private Integer type;

    /**
     * 网站上传者ID
     */
    @TableField("uploader_id")
    private Long uploaderId;

    /**
     * 举报目标ID（type=1时为网站ID，type=2时为评论ID）
     */
    @TableField("website_id")
    private Long websiteId;

    /**
     * 举报原因
     */
    private String reason;

    /**
     * 截图证据（JSON数组）
     */
    private String images;

    /**
     * 处理状态：0=待处理，1=已处理，2=已驳回
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
