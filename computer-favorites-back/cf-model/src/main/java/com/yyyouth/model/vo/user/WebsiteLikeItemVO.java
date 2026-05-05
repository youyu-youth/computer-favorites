package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 我点赞的网站条目
 */
@Data
@Builder
public class WebsiteLikeItemVO {

    /**
     * 点赞记录ID
     */
    private Long id;

    /**
     * 网站ID
     */
    private Long websiteId;

    /**
     * 网站名称
     */
    private String websiteName;

    /**
     * 网站URL
     */
    private String websiteUrl;

    /**
     * 网站图标
     */
    private String websiteIcon;

    /**
     * 网站简介
     */
    private String websiteSummary;

    /**
     * 网站标签
     */
    private List<UserWebsiteTagItemVO> websiteTags;

    /**
     * 网站总点赞数
     */
    private Integer likeCount;

    /**
     * 网站平均评分
     */
    private BigDecimal score;

    /**
     * 点赞时间
     */
    private String likeTime;
}
