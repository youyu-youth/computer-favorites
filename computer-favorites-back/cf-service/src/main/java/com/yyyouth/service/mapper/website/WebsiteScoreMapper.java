package com.yyyouth.service.mapper.website;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.website.WebsiteScore;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 网站评分Mapper
 */
@Mapper
public interface WebsiteScoreMapper extends BaseMapper<WebsiteScore> {

    /**
     * 评分upsert（插入或更新）
     *
     * @param userId 用户ID
     * @param websiteId 网站ID
     * @param score 评分（1-5）
     * @return 影响行数
     */
    @Insert("INSERT INTO t_website_score(user_id, website_id, score, create_time, update_time) " +
            "VALUES(#{userId}, #{websiteId}, #{score}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE score = VALUES(score), update_time = NOW()")
    int upsert(@Param("userId") Long userId,
               @Param("websiteId") Long websiteId,
               @Param("score") Integer score);
}
