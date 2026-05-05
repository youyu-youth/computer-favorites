package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 网站点赞状态
 */
@Data
@Builder
public class WebsiteLikeStatusVO {

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;
}
