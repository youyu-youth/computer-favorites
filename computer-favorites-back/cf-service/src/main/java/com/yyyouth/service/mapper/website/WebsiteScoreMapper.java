package com.yyyouth.service.mapper.website;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.website.WebsiteScore;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 网站评分Mapper
 */
@Mapper
public interface WebsiteScoreMapper extends BaseMapper<WebsiteScore> {

    // 评分仅允许插入一次，重复评分由业务层先查后插控制
}
