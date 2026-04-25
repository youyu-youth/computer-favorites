package com.yyyouth.service.user.announcement.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserAnnouncementQueryDTO;
import com.yyyouth.model.pojo.system.Announcement;
import com.yyyouth.model.vo.user.UserAnnouncementDetailVO;
import com.yyyouth.model.vo.user.UserAnnouncementListItemVO;
import com.yyyouth.model.vo.user.UserAnnouncementPageVO;
import com.yyyouth.service.mapper.system.AnnouncementMapper;
import com.yyyouth.service.user.announcement.UserAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-23
 *
 * 用户端公告服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserAnnouncementServiceImpl implements UserAnnouncementService {

    private static final int NOT_DELETED = 0;

    private static final int VISIBLE_STATUS = 1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AnnouncementMapper announcementMapper;

    /**
     * 查询可见公告分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public UserAnnouncementPageVO queryAnnouncementPage(UserAnnouncementQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        LambdaQueryWrapper<Announcement> baseQueryWrapper = buildUserQueryWrapper(queryDTO);
        long total = announcementMapper.selectCount(baseQueryWrapper);

        UserAnnouncementPageVO pageVO = new UserAnnouncementPageVO();
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));

        if (total == 0) {
            pageVO.setList(Collections.emptyList());
            return pageVO;
        }

        int offset = (pageNum - 1) * pageSize;
        LambdaQueryWrapper<Announcement> listQueryWrapper = buildUserQueryWrapper(queryDTO);
        listQueryWrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreateTime)
                .last("limit " + offset + "," + pageSize);

        List<Announcement> announcementList = announcementMapper.selectList(listQueryWrapper);
        List<UserAnnouncementListItemVO> itemVOS = announcementList.stream()
                .map(announcement -> BeanUtil.copyProperties(announcement, UserAnnouncementListItemVO.class))
                .toList();
        pageVO.setList(itemVOS);
        return pageVO;
    }

    /**
     * 查询可见公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @Override
    public UserAnnouncementDetailVO queryAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .eq(Announcement::getDeleted, NOT_DELETED)
                .eq(Announcement::getStatus, VISIBLE_STATUS)
                .last("limit 1"));

        if (announcement == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "公告不存在或已删除");
        }

        return BeanUtil.copyProperties(announcement, UserAnnouncementDetailVO.class);
    }

    /**
     * 构建用户端查询条件（固定 status=1 且 deleted=0）
     */
    private LambdaQueryWrapper<Announcement> buildUserQueryWrapper(UserAnnouncementQueryDTO queryDTO) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getDeleted, NOT_DELETED)
                .eq(Announcement::getStatus, VISIBLE_STATUS);

        if (queryDTO.getType() != null) {
            queryWrapper.eq(Announcement::getType, queryDTO.getType());
        }

        if (queryDTO.getIsTop() != null) {
            queryWrapper.eq(Announcement::getIsTop, queryDTO.getIsTop());
        }

        return queryWrapper;
    }

    /**
     * 计算总页数
     */
    private long calcTotalPages(long total, int pageSize) {
        if (total == 0) {
            return 0;
        }
        return (total + pageSize - 1L) / pageSize;
    }
}
