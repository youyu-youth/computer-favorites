package com.yyyouth.service.mapper.website;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyyouth zg
 * @date 2026-04-01
 *
 * 网站分类Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<WebsiteCategory> {
}
