package com.yyyouth.service.admin.website;

import com.yyyouth.model.dto.admin.AdminWebsiteCreateDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteQueryDTO;
import com.yyyouth.model.vo.admin.AdminWebsiteCategoryVO;
import com.yyyouth.model.vo.admin.AdminWebsiteLogoUploadVO;
import com.yyyouth.model.vo.admin.AdminWebsitePageVO;
import com.yyyouth.model.vo.admin.AdminWebsiteStatsVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-01
 *
 * 管理端网站治理服务
 */
public interface AdminWebsiteService {

    /**
     * 查询网站分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    AdminWebsitePageVO queryWebsitePage(AdminWebsiteQueryDTO queryDTO);

    /**
     * 查询网站分类统计
     *
     * @param deleted 删除筛选
     * @return 分类统计
     */
    List<AdminWebsiteCategoryVO> queryCategoryStats(Integer deleted);

    /**
     * 查询网站统计数据
     *
     * @param deleted 删除筛选
     * @return 统计数据
     */
    AdminWebsiteStatsVO queryWebsiteStats(Integer deleted);

    /**
     * 上传网站 Logo
     *
     * @param file Logo 文件
     * @return 上传结果
     */
    AdminWebsiteLogoUploadVO uploadWebsiteLogo(MultipartFile file);

    /**
     * 删除网站 Logo
     *
     * @param objectKey 对象键
     */
    void deleteWebsiteLogo(String objectKey);

    /**
     * 创建网站
     *
     * @param createDTO 创建参数
     * @return 新增网站ID
     */
    Long createWebsite(AdminWebsiteCreateDTO createDTO);
}
