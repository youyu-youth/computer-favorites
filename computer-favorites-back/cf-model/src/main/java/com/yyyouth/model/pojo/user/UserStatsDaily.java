package com.yyyouth.model.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户日粒度贡献明细（聚合快照）- user-15 用户主页
 * 唯一索引 (user_id, stat_date)；驱动贡献热力图与趋势折线。
 */
@Data
@TableName("t_user_stats_daily")
public class UserStatsDaily {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("stat_date")
    private LocalDate statDate;

    @TableField("submit_count")
    private Integer submitCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("collect_count")
    private Integer collectCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("score_count")
    private Integer scoreCount;

    @TableField("browse_count")
    private Integer browseCount;

    /** 当日贡献分（单日上限 50，由消费端校验） */
    @TableField("contribution")
    private BigDecimal contribution;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
