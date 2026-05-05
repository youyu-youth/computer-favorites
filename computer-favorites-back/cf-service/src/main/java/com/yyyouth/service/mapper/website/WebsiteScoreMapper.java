package com.yyyouth.service.mapper.website;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.website.WebsiteScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 网站评分Mapper
 */
@Mapper
public interface WebsiteScoreMapper extends BaseMapper<WebsiteScore> {

    // 评分仅允许插入一次，重复评分由业务层先查后插控制

    /**
     * 查询网站评分统计（平均分和评分人数）
     *
     * @param websiteId 网站ID
     * @return avgScore-平均分, scoreCount-评分人数
     */
    @Select("SELECT IFNULL(ROUND(AVG(score), 1), 0) as avgScore, COUNT(*) as scoreCount " +
            "FROM t_website_score WHERE website_id = #{websiteId}")
    Map<String, Object> selectScoreStats(@Param("websiteId") Long websiteId);
}
