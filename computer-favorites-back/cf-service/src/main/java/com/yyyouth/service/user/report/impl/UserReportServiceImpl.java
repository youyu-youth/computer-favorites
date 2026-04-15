package com.yyyouth.service.user.report.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.ReportQueryDTO;
import com.yyyouth.model.dto.user.ReportSubmitDTO;
import com.yyyouth.model.enums.ReportStatus;
import com.yyyouth.model.enums.ReportType;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.pojo.report.Report;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.vo.user.ReportDetailVO;
import com.yyyouth.model.vo.user.ReportListItemVO;
import com.yyyouth.model.vo.user.ReportSubmitVO;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.mapper.report.ReportMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.report.UserReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 用户举报服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserReportServiceImpl implements UserReportService {

    private final ReportMapper reportMapper;
    private final WebsiteMapper websiteMapper;
    private final AdminAccountMapper adminAccountMapper;

    /**
     * 提交举报
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportSubmitVO submitReport(ReportSubmitDTO submitDTO, Long userId) {
        // 1. 校验举报类型
        if (!ReportType.isValid(submitDTO.getType())) {
            throw new BusinessException(40000, "举报类型不正确");
        }

        // 2. 校验目标存在性
        if (ReportType.WEBSITE.getCode().equals(submitDTO.getType())) {
            Website website = websiteMapper.selectById(submitDTO.getTargetId());
            if (website == null) {
                throw new BusinessException(40001, "举报目标不存在");
            }
            if (website.getDeleted() != null && website.getDeleted() == 1) {
                throw new BusinessException(40002, "举报目标已被删除");
            }

            // 3. 自举报拦截
            if (website.getSubmitterId().equals(userId)) {
                throw new BusinessException(40003, "不能举报自己的内容");
            }
        }
        // TODO: type=2 评论举报的校验逻辑（待评论模块实现后补充）

        // 4. 防重复举报检查
        LambdaQueryWrapper<Report> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Report::getUserId, userId)
                .eq(Report::getType, submitDTO.getType())
                .eq(Report::getWebsiteId, submitDTO.getTargetId())
                .eq(Report::getStatus, ReportStatus.PENDING.getCode());
        Long count = reportMapper.selectCount(duplicateWrapper);
        if (count > 0) {
            throw new BusinessException(40004, "您已对该内容提交过举报，请等待处理");
        }

        // 5. 图片URL格式校验
        if (!CollectionUtils.isEmpty(submitDTO.getImages())) {
            for (String imageUrl : submitDTO.getImages()) {
                if (!StringUtils.hasText(imageUrl) || !imageUrl.startsWith("http")) {
                    throw new BusinessException(40005, "图片URL格式不正确");
                }
            }
        }

        // 6. 构建举报记录
        Report report = Report.builder()
                .userId(userId)
                .type(submitDTO.getType())
                .websiteId(submitDTO.getTargetId())
                .reason(submitDTO.getReason())
                .images(CollectionUtils.isEmpty(submitDTO.getImages()) ? null : JSONUtil.toJsonStr(submitDTO.getImages()))
                .status(ReportStatus.PENDING.getCode())
                .build();

        // 7. 保存举报记录
        reportMapper.insert(report);

        log.info("用户提交举报成功，userId={}, reportId={}, type={}, targetId={}",
                userId, report.getId(), submitDTO.getType(), submitDTO.getTargetId());

        // 8. 返回结果
        return ReportSubmitVO.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .createTime(report.getCreateTime())
                .build();
    }

    /**
     * 我的举报列表
     */
    @Override
    public Page<ReportListItemVO> getMyReports(ReportQueryDTO queryDTO, Long userId) {
        // 1. 构建分页查询条件
        Page<Report> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getUserId, userId);

        // 类型筛选
        if (queryDTO.getType() != null && ReportType.isValid(queryDTO.getType())) {
            wrapper.eq(Report::getType, queryDTO.getType());
        }

        // 状态筛选
        if (queryDTO.getStatus() != null && ReportStatus.isValid(queryDTO.getStatus())) {
            wrapper.eq(Report::getStatus, queryDTO.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Report::getCreateTime);

        // 2. 查询举报列表
        Page<Report> reportPage = reportMapper.selectPage(page, wrapper);

        // 3. 转换为VO
        Page<ReportListItemVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        List<ReportListItemVO> voList = reportPage.getRecords().stream().map(report -> {
            ReportListItemVO vo = new ReportListItemVO();
            vo.setId(report.getId());
            vo.setType(report.getType());
            vo.setTargetId(report.getWebsiteId());
            vo.setReason(report.getReason());
            vo.setStatus(report.getStatus());
            vo.setHandleResult(report.getHandleResult());
            vo.setHandleTime(report.getHandleTime());
            vo.setCreateTime(report.getCreateTime());

            // 解析图片JSON
            if (StringUtils.hasText(report.getImages())) {
                vo.setImages(JSONUtil.toList(report.getImages(), String.class));
            }

            // 查询目标名称
            if (ReportType.WEBSITE.getCode().equals(report.getType())) {
                Website website = websiteMapper.selectById(report.getWebsiteId());
                if (website != null) {
                    vo.setTargetName(website.getName());
                }
            }
            // TODO: type=2 评论的目标名称查询（待评论模块实现后补充）

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 举报详情
     */
    @Override
    public ReportDetailVO getReportDetail(Long reportId, Long userId) {
        // 1. 查询举报记录
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(40401, "举报记录不存在");
        }

        // 2. 权限校验：只能查看自己的举报
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(40403, "无权查看此举报");
        }

        // 3. 构建详情VO
        ReportDetailVO vo = new ReportDetailVO();
        vo.setId(report.getId());
        vo.setType(report.getType());
        vo.setTargetId(report.getWebsiteId());
        vo.setReason(report.getReason());
        vo.setStatus(report.getStatus());
        vo.setHandleResult(report.getHandleResult());
        vo.setHandleTime(report.getHandleTime());
        vo.setCreateTime(report.getCreateTime());

        // 解析图片JSON
        if (StringUtils.hasText(report.getImages())) {
            vo.setImages(JSONUtil.toList(report.getImages(), String.class));
        }

        // 4. 查询目标信息
        if (ReportType.WEBSITE.getCode().equals(report.getType())) {
            Website website = websiteMapper.selectById(report.getWebsiteId());
            if (website != null) {
                vo.setTargetName(website.getName());
                vo.setTargetUrl(website.getUrl());
            }
        }
        // TODO: type=2 评论的目标信息查询（待评论模块实现后补充）

        // 5. 查询处理管理员信息
        if (report.getHandlerId() != null) {
            AdminAccount admin = adminAccountMapper.selectById(report.getHandlerId());
            if (admin != null) {
                vo.setHandlerName(admin.getUsername());
            }
        }

        return vo;
    }
}
