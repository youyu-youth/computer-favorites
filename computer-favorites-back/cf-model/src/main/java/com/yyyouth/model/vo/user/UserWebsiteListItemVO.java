package com.yyyouth.model.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站列表项
 */
@Data
public class UserWebsiteListItemVO {

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
     * 评分
     */
    private BigDecimal score;

    /**
     * 标签列表
     */
    private List<UserWebsiteTagItemVO> tags;
}
