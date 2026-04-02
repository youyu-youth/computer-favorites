package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-02
 *
 * 管理端网站详情
 */
@Data
public class AdminWebsiteDetailVO {

    /**
     * 网站ID
     */
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
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

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
     * 标签
     */
    private String tags;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 是否推荐
     */
    private Integer isRecommend;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序值
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
     * 上架时间
     */
    private LocalDateTime shelfTime;

    /**
     * 下架时间
     */
    private LocalDateTime takedownTime;

    /**
     * 审核管理员ID
     */
    private Integer auditAdminId;
}
