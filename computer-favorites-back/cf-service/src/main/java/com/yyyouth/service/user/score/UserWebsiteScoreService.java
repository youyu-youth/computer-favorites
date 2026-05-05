package com.yyyouth.service.user.score;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 用户网站评分服务接口
 */
public interface UserWebsiteScoreService {

    /**
     * 网站评分（upsert）
     *
     * @param websiteId 网站ID
     * @param score 评分（1-5）
     */
    void score(Long websiteId, Integer score);

    /**
     * 查询我的评分
     *
     * @param websiteId 网站ID
     * @return 评分值（1-5），未评过时返回null
     */
    Integer getMyScore(Long websiteId);
}
