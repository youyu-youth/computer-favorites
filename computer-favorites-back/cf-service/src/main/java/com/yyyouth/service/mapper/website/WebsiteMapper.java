package com.yyyouth.service.mapper.website;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.website.Website;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author yyyouth zg
 * @date 2026-04-01
 *
 * 网站Mapper
 */
@Mapper
public interface WebsiteMapper extends BaseMapper<Website> {

    /**
     * 重算网站平均评分和评分人数
     *
     * @param websiteId 网站ID
     * @return 影响行数
     */
    @Update("UPDATE t_website SET score = (SELECT IFNULL(ROUND(AVG(score), 1), 0) " +
            "FROM t_website_score WHERE website_id = #{websiteId}), " +
            "score_count = (SELECT COUNT(*) FROM t_website_score WHERE website_id = #{websiteId}) " +
            "WHERE id = #{websiteId}")
    int recalculateScore(@Param("websiteId") Long websiteId);
}
