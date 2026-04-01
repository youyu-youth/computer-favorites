package com.yyyouth.model.pojo.website;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-01
 *
 * 网站实体
 */
@Data
@TableName("t_website")
public class Website {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 网站名称
     */
    private String name;

    /**
     * 网站URL
     */
    private String url;

    /**
     * 网站图标
     */
    private String icon;

    /**
     * 网站简介
     */
    private String summary;

    /**
     * 网站描述
     */
    private String description;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 点击量
     */
    private Integer clickCount;

    /**
     * 点赞量
     */
    private Integer likeCount;

    /**
     * 收藏量
     */
    private Integer collectCount;

    /**
     * 评论量
     */
    private Integer commentCount;

    /**
     * 平均评分
     */
    private BigDecimal score;

    /**
     * 评分人数
     */
    private Integer scoreCount;

    /**
     * 标签列表
     */
    private String tags;

    /**
     * 是否置顶
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 是否推荐
     */
    @TableField("is_recommend")
    private Integer isRecommend;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 来源
     */
    private Integer source;

    /**
     * 提交用户ID
     */
    private Long submitterId;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private Integer deleted;

    /**
     * 审核管理员ID
     */
    @TableField("audit_admin_id")
    private Integer auditAdminId;
}
