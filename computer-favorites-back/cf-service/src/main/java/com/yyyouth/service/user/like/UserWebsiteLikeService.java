package com.yyyouth.service.user.like;

import com.yyyouth.model.dto.user.WebsiteLikePageDTO;
import com.yyyouth.model.vo.user.WebsiteLikePageVO;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 用户网站点赞服务接口
 */
public interface UserWebsiteLikeService {

    /**
     * 网站点赞
     *
     * @param websiteId 网站ID
     */
    void like(Long websiteId);

    /**
     * 取消点赞
     *
     * @param websiteId 网站ID
     */
    void unlike(Long websiteId);

    /**
     * 查询当前用户是否已点赞
     *
     * @param websiteId 网站ID
     * @return 是否已点赞
     */
    Boolean isLiked(Long websiteId);

    /**
     * 我点赞的网站列表（分页）
     *
     * @param pageDTO 分页参数
     * @return 分页结果
     */
    WebsiteLikePageVO pageMyLikes(WebsiteLikePageDTO pageDTO);
}
