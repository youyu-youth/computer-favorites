package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 我的网站评分
 */
@Data
@Builder
public class WebsiteScoreMineVO {

    /**
     * 评分（1-5星，未评过时为null）
     */
    private Integer score;
}
