package com.yyyouth.service.admin.announcement.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminAnnouncementCreateDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementEditDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementQueryDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementStatusDTO;
import com.yyyouth.model.pojo.system.Announcement;
import com.yyyouth.model.vo.admin.AdminAnnouncementListItemVO;
import com.yyyouth.model.vo.admin.AdminAnnouncementPageVO;
import com.yyyouth.model.vo.admin.AdminAnnouncementStatsVO;
import com.yyyouth.service.admin.announcement.AdminAnnouncementService;
import com.yyyouth.service.mapper.system.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author yyyouth zg
 * @date 2026-04-22
 *
 * 管理端公告服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    private static final int NOT_DELETED = 0;

    private static final int DELETED = 1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 6;

    private final AnnouncementMapper announcementMapper;

    /**
     * 查询公告分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据（含统计信息）
     */
    @Override
    public AdminAnnouncementPageVO queryAnnouncementPage(AdminAnnouncementQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        LambdaQueryWrapper<Announcement> baseQueryWrapper = buildQueryWrapper(queryDTO);
        long total = announcementMapper.selectCount(baseQueryWrapper);

        AdminAnnouncementPageVO pageVO = new AdminAnnouncementPageVO();
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));

        AdminAnnouncementStatsVO statsVO = buildStats();
        pageVO.setStats(statsVO);

        if (total == 0) {
            pageVO.setRecords(Collections.emptyList());
            return pageVO;
        }

        int offset = (pageNum - 1) * pageSize;
        LambdaQueryWrapper<Announcement> listQueryWrapper = buildQueryWrapper(queryDTO);
        listQueryWrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreateTime)
                .last("limit " + offset + "," + pageSize);

        List<Announcement> announcementList = announcementMapper.selectList(listQueryWrapper);
        List<AdminAnnouncementListItemVO> itemVOS = announcementList.stream()
                .map(announcement -> BeanUtil.copyProperties(announcement, AdminAnnouncementListItemVO.class))
                .toList();
        pageVO.setRecords(itemVOS);
        return pageVO;
    }

    /**
     * 创建公告
     *
     * @param createDTO 创建参数
     * @return 公告ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAnnouncement(AdminAnnouncementCreateDTO createDTO) {
        String normalizedTitle = normalizeTitle(createDTO.getTitle());

        validateTitleUniqueness(normalizedTitle, null);

        Announcement announcement = new Announcement();
        LocalDateTime now = LocalDateTime.now();
        announcement.setTitle(normalizedTitle);
        announcement.setContent(createDTO.getContent().trim());
        announcement.setType(createDTO.getType());
        announcement.setIsTop(createDTO.getIsTop() != null ? createDTO.getIsTop() : 0);
        announcement.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 1);
        announcement.setPublishTime(createDTO.getPublishTime());
        announcement.setDeleted(NOT_DELETED);
        announcement.setCreateTime(now);
        announcement.setUpdateTime(now);

        int insertedRows = announcementMapper.insert(announcement);
        if (insertedRows != 1 || announcement.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "公告创建失败，请稍后重试");
        }
        return announcement.getId();
    }

    /**
     * 编辑公告
     *
     * @param id 公告ID
     * @param editDTO 编辑参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editAnnouncement(Long id, AdminAnnouncementEditDTO editDTO) {
        Announcement current = queryAvailableAnnouncementById(id);

        String normalizedTitle = normalizeTitle(editDTO.getTitle());
        if (!Objects.equals(current.getTitle(), normalizedTitle)) {
            validateTitleUniqueness(normalizedTitle, id);
        }

        int updatedRows = announcementMapper.update(new Announcement(), new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .eq(Announcement::getDeleted, NOT_DELETED)
                .set(Announcement::getTitle, normalizedTitle)
                .set(Announcement::getContent, editDTO.getContent().trim())
                .set(Announcement::getType, editDTO.getType())
                .set(Announcement::getIsTop, editDTO.getIsTop() != null ? editDTO.getIsTop() : 0)
                .set(Announcement::getStatus, editDTO.getStatus() != null ? editDTO.getStatus() : 1)
                .set(Announcement::getPublishTime, editDTO.getPublishTime())
                .set(Announcement::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "公告更新失败，请稍后重试");
        }
    }

    /**
     * 更新公告状态
     *
     * @param id 公告ID
     * @param statusDTO 状态参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnouncementStatus(Long id, AdminAnnouncementStatusDTO statusDTO) {
        Announcement current = queryAvailableAnnouncementById(id);

        if (Objects.equals(current.getStatus(), statusDTO.getStatus())) {
            return;
        }

        int updatedRows = announcementMapper.update(new Announcement(), new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .eq(Announcement::getDeleted, NOT_DELETED)
                .set(Announcement::getStatus, statusDTO.getStatus())
                .set(Announcement::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "公告状态更新失败，请稍后重试");
        }
    }

    /**
     * 删除公告（逻辑删除）
     *
     * @param id 公告ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Long id) {
        queryAvailableAnnouncementById(id);

        int updatedRows = announcementMapper.update(new Announcement(), new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .eq(Announcement::getDeleted, NOT_DELETED)
                .set(Announcement::getDeleted, DELETED)
                .set(Announcement::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "公告删除失败，请刷新后重试");
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Announcement> buildQueryWrapper(AdminAnnouncementQueryDTO queryDTO) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getDeleted, NOT_DELETED);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(Announcement::getTitle, keyword)
                    .or()
                    .like(Announcement::getContent, keyword));
        }

        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Announcement::getStatus, queryDTO.getStatus());
        }

        if (queryDTO.getType() != null) {
            queryWrapper.eq(Announcement::getType, queryDTO.getType());
        }

        if (queryDTO.getIsTop() != null) {
            queryWrapper.eq(Announcement::getIsTop, queryDTO.getIsTop());
        }

        return queryWrapper;
    }

    /**
     * 构建统计信息
     */
    private AdminAnnouncementStatsVO buildStats() {
        long total = countByCondition(null);
        long visible = countByCondition(wrapper -> wrapper.eq(Announcement::getStatus, 1));
        long hidden = countByCondition(wrapper -> wrapper.eq(Announcement::getStatus, 0));
        long top = countByCondition(wrapper -> wrapper.eq(Announcement::getIsTop, 1));

        AdminAnnouncementStatsVO statsVO = new AdminAnnouncementStatsVO();
        statsVO.setTotal(total);
        statsVO.setVisible(visible);
        statsVO.setHidden(hidden);
        statsVO.setTop(top);
        return statsVO;
    }

    /**
     * 查询可操作公告
     */
    private Announcement queryAvailableAnnouncementById(Long id) {
        Announcement announcement = announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .eq(Announcement::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (announcement == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "公告不存在或已删除");
        }
        return announcement;
    }

    /**
     * 校验标题唯一性
     */
    private void validateTitleUniqueness(String title, Long excludeId) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getTitle, title)
                .eq(Announcement::getDeleted, NOT_DELETED);
        if (excludeId != null) {
            queryWrapper.ne(Announcement::getId, excludeId);
        }
        queryWrapper.last("limit 1");

        Announcement existed = announcementMapper.selectOne(queryWrapper);
        if (existed != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "公告标题已存在");
        }
    }

    /**
     * 统计公告数量
     */
    private long countByCondition(Consumer<LambdaQueryWrapper<Announcement>> conditionAppender) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getDeleted, NOT_DELETED);
        if (conditionAppender != null) {
            conditionAppender.accept(queryWrapper);
        }
        return announcementMapper.selectCount(queryWrapper);
    }

    /**
     * 标准化标题
     */
    private String normalizeTitle(String title) {
        return title == null ? "" : title.trim();
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
