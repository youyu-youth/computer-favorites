package com.yyyouth.service.admin.announcement;

import com.yyyouth.model.dto.admin.AdminAnnouncementCreateDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementEditDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementQueryDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementStatusDTO;
import com.yyyouth.model.vo.admin.AdminAnnouncementPageVO;

/**
 * @author yyyouth zg
 * @date 2026-04-22
 *
 * 管理端公告服务接口
 */
public interface AdminAnnouncementService {

    /**
     * 查询公告分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据（含统计信息）
     */
    AdminAnnouncementPageVO queryAnnouncementPage(AdminAnnouncementQueryDTO queryDTO);

    /**
     * 创建公告
     *
     * @param createDTO 创建参数
     * @return 公告ID
     */
    Long createAnnouncement(AdminAnnouncementCreateDTO createDTO);

    /**
     * 编辑公告
     *
     * @param id 公告ID
     * @param editDTO 编辑参数
     */
    void editAnnouncement(Long id, AdminAnnouncementEditDTO editDTO);

    /**
     * 更新公告状态
     *
     * @param id 公告ID
     * @param statusDTO 状态参数
     */
    void updateAnnouncementStatus(Long id, AdminAnnouncementStatusDTO statusDTO);

    /**
     * 删除公告（逻辑删除）
     *
     * @param id 公告ID
     */
    void deleteAnnouncement(Long id);
}
