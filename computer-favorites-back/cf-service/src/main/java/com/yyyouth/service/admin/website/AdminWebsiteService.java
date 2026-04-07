package com.yyyouth.service.admin.website;

import com.yyyouth.model.dto.admin.AdminWebsiteBatchStatusUpdateDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteBatchAuditDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteAuditDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteCreateDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteEditDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteQueryDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteStatusUpdateDTO;
import com.yyyouth.model.vo.admin.AdminWebsiteBatchAuditResultVO;
import com.yyyouth.model.vo.admin.AdminWebsiteCategoryVO;
import com.yyyouth.model.vo.admin.AdminWebsiteDetailVO;
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
     * 查询网站详情
     *
     * @param websiteId 网站ID
     * @return 网站详情
     */
    AdminWebsiteDetailVO queryWebsiteDetail(Long websiteId);

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

    /**
     * 编辑网站
     *
     * @param websiteId 网站ID
     * @param editDTO 编辑参数
     */
    void editWebsite(Long websiteId, AdminWebsiteEditDTO editDTO);

    /**
     * 删除网站（逻辑删除）
     *
     * @param websiteId 网站ID
     */
    void deleteWebsite(Long websiteId);

    /**
     * 更新网站上架状态
     *
     * @param updateDTO 状态更新参数
     */
    void updateWebsiteStatus(AdminWebsiteStatusUpdateDTO updateDTO);

    /**
     * 批量更新网站上架状态
     *
     * @param updateDTO 批量状态更新参数
     * @return 实际更新数量
     */
    int batchUpdateWebsiteStatus(AdminWebsiteBatchStatusUpdateDTO updateDTO);

    /**
     * 审核网站
     *
     * @param websiteId 网站ID
     * @param auditDTO 审核参数
     */
    void auditWebsite(Long websiteId, AdminWebsiteAuditDTO auditDTO);

    /**
     * 批量审核网站
     *
     * @param batchAuditDTO 批量审核参数
     * @return 批量审核结果
     */
    AdminWebsiteBatchAuditResultVO batchAuditWebsite(AdminWebsiteBatchAuditDTO batchAuditDTO);
}
