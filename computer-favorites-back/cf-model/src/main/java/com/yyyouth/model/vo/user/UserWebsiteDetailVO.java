package com.yyyouth.model.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站详情
 */
@Data
public class UserWebsiteDetailVO {

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
     * 当前用户是否已收藏（未登录时为false）
     */
    private Boolean isCollected;

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
    private List<UserWebsiteTagItemVO> tags;

    /**
     * 是否官方网站
     */
    private Integer isOfficial;

    /**
     * 是否推荐
     */
    private Integer isRecommend;

    /**
     * 提交用户ID
     */
    private Long submitterId;

    /**
     * 提供者名称
     */
    private String providerName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 上架时间
     */
    private LocalDateTime shelfTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
