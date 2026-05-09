package com.yyyouth.model.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户标签 / 分类 / 技术栈偏好快照 - user-15 用户主页
 * 唯一索引 (user_id, dim_type, dim_id)；驱动分类占比与技术栈雷达。
 */
@Data
@TableName("t_user_tag_affinity")
public class UserTagAffinity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    /** 维度类型：1-分类 2-标签 3-技术栈 */
    @TableField("dim_type")
    private Integer dimType;

    @TableField("dim_id")
    private Long dimId;

    /** 维度名称（冗余，免 JOIN） */
    @TableField("dim_name")
    private String dimName;

    /** 偏好权重（收藏*3 + 点赞*1 + 浏览*0.3） */
    @TableField("weight")
    private BigDecimal weight;

    @TableField("hits")
    private Integer hits;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
