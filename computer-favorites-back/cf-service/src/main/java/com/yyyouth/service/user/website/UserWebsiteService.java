package com.yyyouth.service.user.website;

import com.yyyouth.model.dto.user.UserWebsiteQueryDTO;
import com.yyyouth.model.vo.user.UserWebsiteCategoryVO;
import com.yyyouth.model.vo.user.UserWebsiteDetailVO;
import com.yyyouth.model.vo.user.UserWebsitePageVO;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站资源服务
 */
public interface UserWebsiteService {

    /**
     * 查询网站分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    UserWebsitePageVO queryWebsitePage(UserWebsiteQueryDTO queryDTO);

    /**
     * 查询网站分类统计
     *
     * @return 分类统计列表
     */
    List<UserWebsiteCategoryVO> queryCategoryStats();

    /**
     * 查询网站详情
     *
     * @param websiteId 网站ID
     * @return 网站详情
     */
    UserWebsiteDetailVO queryWebsiteDetail(Long websiteId);

    /**
     * 增加网站点击量
     *
     * @param websiteId 网站ID
     */
    void incrementClickCount(Long websiteId);
}
