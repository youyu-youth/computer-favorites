package com.yyyouth.service.user.announcement;

import com.yyyouth.model.dto.user.UserAnnouncementQueryDTO;
import com.yyyouth.model.vo.user.UserAnnouncementDetailVO;
import com.yyyouth.model.vo.user.UserAnnouncementPageVO;

/**
 * @author yyyouth zg
 * @date 2026-04-23
 *
 * 用户端公告服务接口
 */
public interface UserAnnouncementService {

    /**
     * 查询可见公告分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    UserAnnouncementPageVO queryAnnouncementPage(UserAnnouncementQueryDTO queryDTO);

    /**
     * 查询可见公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    UserAnnouncementDetailVO queryAnnouncementDetail(Long id);
}
