package com.yyyouth.service.admin.report.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminReportBatchHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportQueryDTO;
import com.yyyouth.model.enums.ReportStatus;
import com.yyyouth.model.enums.ReportType;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.report.Report;
import com.yyyouth.model.pojo.website.Comment;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.vo.admin.AdminReportBatchHandleResultItemVO;
import com.yyyouth.model.vo.admin.AdminReportBatchHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportDetailTimelineItemVO;
import com.yyyouth.model.vo.admin.AdminReportDetailVO;
import com.yyyouth.model.vo.admin.AdminReportHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportListItemVO;
import com.yyyouth.model.vo.admin.AdminReportPageVO;
import com.yyyouth.model.vo.admin.AdminReportStatisticsVO;
import com.yyyouth.service.admin.report.AdminReportService;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.mapper.report.ReportMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.website.CommentMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报处置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private static final int WEBSITE_OFFLINE_STATUS = 0;

    private static final int COMMENT_HIDDEN_STATUS = 0;

    private static final int NOT_DELETED = 0;

    private static final String ACTION_PASS = "pass";

    private static final String ACTION_REJECT = "reject";

    private static final String TIMELINE_DONE = "done";

    private static final String TIMELINE_PENDING = "pending";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String WEBSITE_ONLINE_LABEL = "在线可访问";

    private static final String WEBSITE_OFFLINE_LABEL = "已下架";

    private static final String COMMENT_VISIBLE_LABEL = "评论可见";

    private static final String COMMENT_HIDDEN_LABEL = "评论已隐藏";

    private static final String TARGET_MISSING_LABEL = "目标已删除或不可访问";

    private final ReportMapper reportMapper;

    private final WebsiteMapper websiteMapper;

    private final CommentMapper commentMapper;

    private final UserAccountMapper userAccountMapper;

    private final AdminAccountMapper adminAccountMapper;

    private final TransactionTemplate transactionTemplate;

    /**
     * 查询举报分页
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @Override
    public AdminReportPageVO queryReportPage(AdminReportQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 6 : queryDTO.getPageSize();

        LambdaQueryWrapper<Report> wrapper = buildReportQueryWrapper(queryDTO);
        wrapper.orderByDesc(Report::getCreateTime);

        List<Report> reportList = reportMapper.selectList(wrapper);
        List<AdminReportListItemVO> records = enrichReportList(reportList);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            records = records.stream()
                    .filter(item -> matchKeyword(item, queryDTO.getKeyword()))
                    .toList();
        }

        long total = records.size();
        List<AdminReportListItemVO> pagedRecords = paginateRecords(records, pageNum, pageSize);

        AdminReportPageVO pageVO = new AdminReportPageVO();
        pageVO.setRecords(pagedRecords);
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));
        return pageVO;
    }

    /**
     * 查询举报详情
     *
     * @param reportId 举报ID
     * @return 举报详情
     */
    @Override
    public AdminReportDetailVO queryReportDetail(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(40401, "举报记录不存在");
        }

        TargetContext targetContext = resolveTargetContext(report);
        Long uploaderId = targetContext.uploaderId();

        Set<Long> userIds = new LinkedHashSet<>();
        userIds.add(report.getUserId());
        if (uploaderId != null) {
            userIds.add(uploaderId);
        }
        Map<Long, UserAccount> userMap = buildUserMap(userIds);
        AdminAccount handler = report.getHandlerId() == null ? null : adminAccountMapper.selectById(report.getHandlerId());

        AdminReportDetailVO detailVO = new AdminReportDetailVO();
        fillCommonFields(detailVO, report, userMap.get(report.getUserId()), targetContext, userMap.get(uploaderId), handler);
        detailVO.setTargetStatusLabel(targetContext.targetStatusLabel());
        detailVO.setEvidenceSummary(buildEvidenceSummary(detailVO.getImages()));
        detailVO.setTimeline(buildTimeline(detailVO));
        return detailVO;
    }

    /**
     * 单条处置举报
     *
     * @param reportId 举报ID
     * @param handleDTO 处置参数
     * @return 处置结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminReportHandleResultVO handleReport(Long reportId, AdminReportHandleDTO handleDTO) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(40401, "举报记录不存在");
        }
        if (!ReportStatus.PENDING.getCode().equals(report.getStatus())) {
            throw new BusinessException(40402, "举报已被处置");
        }

        Long adminId = StpAdminUtil.getLoginIdAsLong();
        LocalDateTime now = LocalDateTime.now();
        Integer nextStatus = ACTION_PASS.equals(handleDTO.getAction())
                ? ReportStatus.PROCESSED.getCode()
                : ReportStatus.REJECTED.getCode();

        Report updateEntity = Report.builder()
                .id(reportId)
                .status(nextStatus)
                .handleResult(handleDTO.getHandleResult())
                .handlerId(adminId)
                .handleTime(now)
                .updateTime(now)
                .build();
        LambdaUpdateWrapper<Report> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Report::getId, reportId)
                .eq(Report::getStatus, ReportStatus.PENDING.getCode());
        int updated = reportMapper.update(updateEntity, updateWrapper);
        if (updated <= 0) {
            throw new BusinessException(40402, "举报已被处置");
        }

        boolean actionExecuted = false;
        if (ACTION_PASS.equals(handleDTO.getAction()) && Boolean.TRUE.equals(handleDTO.getExecuteAction())) {
            actionExecuted = executeGovernanceAction(report);
            if (!actionExecuted) {
                throw new BusinessException(40403, "目标联动处置失败");
            }
        }

        AdminReportHandleResultVO resultVO = new AdminReportHandleResultVO();
        resultVO.setReportId(reportId);
        resultVO.setStatus(nextStatus);
        resultVO.setHandleResult(handleDTO.getHandleResult());
        resultVO.setHandleTime(now);
        resultVO.setActionExecuted(actionExecuted);
        return resultVO;
    }

    /**
     * 批量处置举报
     *
     * @param batchHandleDTO 批量处置参数
     * @return 批量处置结果
     */
    @Override
    public AdminReportBatchHandleResultVO batchHandleReports(AdminReportBatchHandleDTO batchHandleDTO) {
        List<AdminReportBatchHandleResultItemVO> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (Long reportId : batchHandleDTO.getReportIds()) {
            AdminReportBatchHandleResultItemVO itemVO = new AdminReportBatchHandleResultItemVO();
            itemVO.setReportId(reportId);
            try {
                AdminReportHandleDTO handleDTO = new AdminReportHandleDTO();
                handleDTO.setAction(batchHandleDTO.getAction());
                handleDTO.setHandleResult(batchHandleDTO.getHandleResult());
                handleDTO.setExecuteAction(batchHandleDTO.getExecuteAction());
                transactionTemplate.execute(status -> {
                    handleReport(reportId, handleDTO);
                    return null;
                });
                itemVO.setSuccess(Boolean.TRUE);
                itemVO.setMessage("处置成功");
                successCount++;
            } catch (BusinessException ex) {
                itemVO.setSuccess(Boolean.FALSE);
                itemVO.setMessage(ex.getMessage());
                failedCount++;
            }
            results.add(itemVO);
        }

        AdminReportBatchHandleResultVO resultVO = new AdminReportBatchHandleResultVO();
        resultVO.setTotal(batchHandleDTO.getReportIds().size());
        resultVO.setSuccess(successCount);
        resultVO.setFailed(failedCount);
        resultVO.setResults(results);
        return resultVO;
    }

    /**
     * 查询举报统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @Override
    public AdminReportStatisticsVO queryReportStatistics(String startTime, String endTime) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        LocalDateTime parsedStartTime = parseTime(startTime);
        LocalDateTime parsedEndTime = parseTime(endTime);
        if (parsedStartTime != null) {
            wrapper.ge(Report::getCreateTime, parsedStartTime);
        }
        if (parsedEndTime != null) {
            wrapper.le(Report::getCreateTime, parsedEndTime);
        }

        List<Report> reportList = reportMapper.selectList(wrapper);
        int total = reportList.size();
        int pending = countByStatus(reportList, ReportStatus.PENDING.getCode());
        int processed = countByStatus(reportList, ReportStatus.PROCESSED.getCode());
        int rejected = countByStatus(reportList, ReportStatus.REJECTED.getCode());
        int websiteCount = countByType(reportList, ReportType.WEBSITE.getCode());
        int commentCount = countByType(reportList, ReportType.COMMENT.getCode());
        LocalDateTime last24HourMark = LocalDateTime.now().minusHours(24);
        int last24Hours = (int) reportList.stream()
                .filter(report -> report.getCreateTime() != null && !report.getCreateTime().isBefore(last24HourMark))
                .count();

        AdminReportStatisticsVO statisticsVO = new AdminReportStatisticsVO();
        statisticsVO.setTotal(total);
        statisticsVO.setPending(pending);
        statisticsVO.setProcessed(processed);
        statisticsVO.setRejected(rejected);
        statisticsVO.setLast24Hours(last24Hours);
        statisticsVO.setWebsiteCount(websiteCount);
        statisticsVO.setCommentCount(commentCount);
        statisticsVO.setProcessRate(total == 0 ? 0 : Math.round(((processed + rejected) * 100.0F) / total));
        return statisticsVO;
    }

    /**
     * 构建举报查询条件
     *
     * @param queryDTO 查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<Report> buildReportQueryWrapper(AdminReportQueryDTO queryDTO) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(queryDTO.getPendingOnly())) {
            wrapper.eq(Report::getStatus, ReportStatus.PENDING.getCode());
        } else if (queryDTO.getStatus() != null) {
            wrapper.eq(Report::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getType() != null) {
            wrapper.eq(Report::getType, queryDTO.getType());
        }
        LocalDateTime startTime = parseTime(queryDTO.getStartTime());
        LocalDateTime endTime = parseTime(queryDTO.getEndTime());
        if (startTime != null) {
            wrapper.ge(Report::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Report::getCreateTime, endTime);
        }
        return wrapper;
    }

    /**
     * 批量补齐举报列表显示字段
     *
     * @param reportList 举报列表
     * @return 展示列表
     */
    private List<AdminReportListItemVO> enrichReportList(List<Report> reportList) {
        if (reportList == null || reportList.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> websiteIds = new LinkedHashSet<>();
        Set<Long> commentIds = new LinkedHashSet<>();
        Set<Long> userIds = new LinkedHashSet<>();
        Set<Long> handlerIds = new LinkedHashSet<>();

        for (Report report : reportList) {
            userIds.add(report.getUserId());
            if (report.getUploaderId() != null) {
                userIds.add(report.getUploaderId());
            }
            if (report.getHandlerId() != null) {
                handlerIds.add(report.getHandlerId());
            }
            if (ReportType.WEBSITE.getCode().equals(report.getType())) {
                websiteIds.add(report.getWebsiteId());
            } else if (ReportType.COMMENT.getCode().equals(report.getType())) {
                commentIds.add(report.getWebsiteId());
            }
        }

        Map<Long, Website> websiteMap = buildWebsiteMap(websiteIds);
        Map<Long, Comment> commentMap = buildCommentMap(commentIds);
        for (Website website : websiteMap.values()) {
            if (website.getSubmitterId() != null) {
                userIds.add(website.getSubmitterId());
            }
        }
        for (Comment comment : commentMap.values()) {
            if (comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
        }

        Map<Long, UserAccount> userMap = buildUserMap(userIds);
        Map<Long, AdminAccount> adminMap = buildAdminMap(handlerIds);

        List<AdminReportListItemVO> list = new ArrayList<>();
        for (Report report : reportList) {
            TargetContext targetContext = resolveTargetContext(report, websiteMap, commentMap);
            Long uploaderId = targetContext.uploaderId();
            UserAccount uploader = uploaderId == null ? null : userMap.get(uploaderId);
            AdminReportListItemVO itemVO = new AdminReportListItemVO();
            fillCommonFields(itemVO, report, userMap.get(report.getUserId()), targetContext, uploader, adminMap.get(report.getHandlerId()));
            list.add(itemVO);
        }
        return list;
    }

    /**
     * 填充列表/详情通用字段
     *
     * @param itemVO 目标对象
     * @param report 举报实体
     * @param reporter 举报人
     * @param targetContext 目标信息
     * @param uploader 发布人
     * @param handler 处理管理员
     */
    private void fillCommonFields(AdminReportListItemVO itemVO,
                                  Report report,
                                  UserAccount reporter,
                                  TargetContext targetContext,
                                  UserAccount uploader,
                                  AdminAccount handler) {
        itemVO.setId(report.getId());
        itemVO.setUserId(report.getUserId());
        itemVO.setUserName(resolveUserName(reporter));
        itemVO.setUserEmail(reporter == null ? null : reporter.getEmail());
        itemVO.setType(report.getType());
        itemVO.setTargetId(report.getWebsiteId());
        itemVO.setTargetName(targetContext.targetName());
        itemVO.setTargetUrl(targetContext.targetUrl());
        itemVO.setUploaderId(targetContext.uploaderId());
        itemVO.setUploaderName(resolveUserName(uploader));
        itemVO.setReason(report.getReason());
        itemVO.setImages(parseImages(report.getImages()));
        itemVO.setStatus(report.getStatus());
        itemVO.setHandleResult(report.getHandleResult());
        itemVO.setHandlerId(report.getHandlerId());
        itemVO.setHandlerName(handler == null ? null : handler.getUsername());
        itemVO.setHandleTime(report.getHandleTime());
        itemVO.setCreateTime(report.getCreateTime());
        itemVO.setUpdateTime(report.getUpdateTime());
    }

    /**
     * 解析目标信息
     *
     * @param report 举报实体
     * @return 目标信息
     */
    private TargetContext resolveTargetContext(Report report) {
        if (ReportType.WEBSITE.getCode().equals(report.getType())) {
            Website website = websiteMapper.selectById(report.getWebsiteId());
            Map<Long, Website> websiteMap = new HashMap<>();
            if (website != null) {
                websiteMap.put(website.getId(), website);
            }
            return resolveTargetContext(report, websiteMap, Collections.emptyMap());
        }

        Comment comment = commentMapper.selectById(report.getWebsiteId());
        Map<Long, Comment> commentMap = new HashMap<>();
        if (comment != null) {
            commentMap.put(comment.getId(), comment);
        }
        return resolveTargetContext(report, Collections.emptyMap(), commentMap);
    }

    /**
     * 使用缓存目标映射解析目标信息
     *
     * @param report 举报实体
     * @param websiteMap 网站映射
     * @param commentMap 评论映射
     * @return 目标信息
     */
    private TargetContext resolveTargetContext(Report report, Map<Long, Website> websiteMap, Map<Long, Comment> commentMap) {
        if (ReportType.WEBSITE.getCode().equals(report.getType())) {
            Website website = websiteMap.get(report.getWebsiteId());
            if (website == null) {
                return new TargetContext("网站 #" + report.getWebsiteId(), null, report.getUploaderId(), TARGET_MISSING_LABEL);
            }
            return new TargetContext(
                    website.getName(),
                    website.getUrl(),
                    report.getUploaderId() != null ? report.getUploaderId() : website.getSubmitterId(),
                    resolveWebsiteStatusLabel(website)
            );
        }

        Comment comment = commentMap.get(report.getWebsiteId());
        if (comment == null) {
            return new TargetContext("评论 #" + report.getWebsiteId(), null, report.getUploaderId(), TARGET_MISSING_LABEL);
        }
        String summary = buildCommentSummary(comment.getContent());
        return new TargetContext(
                "评论 #" + report.getWebsiteId() + "：" + summary,
                null,
                report.getUploaderId() != null ? report.getUploaderId() : comment.getUserId(),
                resolveCommentStatusLabel(comment)
        );
    }

    /**
     * 构建详情时间线
     *
     * @param detailVO 举报详情
     * @return 时间线
     */
    private List<AdminReportDetailTimelineItemVO> buildTimeline(AdminReportDetailVO detailVO) {
        List<AdminReportDetailTimelineItemVO> timeline = new ArrayList<>();

        AdminReportDetailTimelineItemVO createItem = new AdminReportDetailTimelineItemVO();
        createItem.setId("create-" + detailVO.getId());
        createItem.setTitle("举报已提交");
        createItem.setDescription((detailVO.getUserName() == null ? "用户" : detailVO.getUserName())
                + " 提交了" + buildReportTypeLabel(detailVO.getType()) + "，等待管理员处理。");
        createItem.setTime(formatTime(detailVO.getCreateTime()));
        createItem.setTone(TIMELINE_DONE);
        timeline.add(createItem);

        AdminReportDetailTimelineItemVO handleItem = new AdminReportDetailTimelineItemVO();
        if (ReportStatus.PENDING.getCode().equals(detailVO.getStatus())) {
            handleItem.setId("pending-" + detailVO.getId());
            handleItem.setTitle("等待处置");
            handleItem.setDescription("案件仍处于待处理状态，建议优先查看证据截图与目标对象信息。");
            handleItem.setTime(formatTime(detailVO.getUpdateTime() == null ? detailVO.getCreateTime() : detailVO.getUpdateTime()));
            handleItem.setTone(TIMELINE_PENDING);
        } else {
            handleItem.setId("handled-" + detailVO.getId());
            handleItem.setTitle(ReportStatus.PROCESSED.getCode().equals(detailVO.getStatus()) ? "举报已通过" : "举报已驳回");
            handleItem.setDescription(StringUtils.hasText(detailVO.getHandleResult()) ? detailVO.getHandleResult() : "管理员已完成处置。");
            handleItem.setTime(formatTime(detailVO.getHandleTime() == null ? detailVO.getUpdateTime() : detailVO.getHandleTime()));
            handleItem.setTone(TIMELINE_DONE);
        }
        timeline.add(handleItem);
        return timeline;
    }

    /**
     * 执行联动治理动作
     *
     * @param report 举报记录
     * @return 是否执行成功
     */
    private boolean executeGovernanceAction(Report report) {
        if (ReportType.WEBSITE.getCode().equals(report.getType())) {
            Website website = websiteMapper.selectById(report.getWebsiteId());
            if (website == null || (website.getDeleted() != null && !Objects.equals(website.getDeleted(), NOT_DELETED))) {
                return false;
            }
            if (Objects.equals(website.getStatus(), WEBSITE_OFFLINE_STATUS)) {
                return true;
            }
            Website updateWebsite = new Website();
            updateWebsite.setId(website.getId());
            updateWebsite.setStatus(WEBSITE_OFFLINE_STATUS);
            updateWebsite.setTakedownTime(LocalDateTime.now());
            return websiteMapper.updateById(updateWebsite) > 0;
        }

        if (ReportType.COMMENT.getCode().equals(report.getType())) {
            Comment comment = commentMapper.selectById(report.getWebsiteId());
            if (comment == null || (comment.getDeleted() != null && !Objects.equals(comment.getDeleted(), NOT_DELETED))) {
                return false;
            }
            if (Objects.equals(comment.getStatus(), COMMENT_HIDDEN_STATUS)) {
                return true;
            }
            Comment updateComment = new Comment();
            updateComment.setId(comment.getId());
            updateComment.setStatus(COMMENT_HIDDEN_STATUS);
            return commentMapper.updateById(updateComment) > 0;
        }
        return false;
    }

    /**
     * 解析图片JSON
     *
     * @param images 图片JSON
     * @return 图片列表
     */
    private List<String> parseImages(String images) {
        if (!StringUtils.hasText(images)) {
            return Collections.emptyList();
        }
        return JSONUtil.toList(images, String.class);
    }

    /**
     * 构建证据摘要
     *
     * @param images 图片列表
     * @return 证据摘要
     */
    private String buildEvidenceSummary(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "当前未上传截图证据，建议结合目标对象内容进行人工复核。";
        }
        return "已提交 " + images.size() + " 张截图证据，可用于辅助判定。";
    }

    private String resolveWebsiteStatusLabel(Website website) {
        if (website == null) {
            return TARGET_MISSING_LABEL;
        }
        return Objects.equals(website.getStatus(), WEBSITE_OFFLINE_STATUS) ? WEBSITE_OFFLINE_LABEL : WEBSITE_ONLINE_LABEL;
    }

    private String resolveCommentStatusLabel(Comment comment) {
        if (comment == null) {
            return TARGET_MISSING_LABEL;
        }
        return Objects.equals(comment.getStatus(), COMMENT_HIDDEN_STATUS) ? COMMENT_HIDDEN_LABEL : COMMENT_VISIBLE_LABEL;
    }

    private List<AdminReportListItemVO> paginateRecords(List<AdminReportListItemVO> records, int pageNum, int pageSize) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = (safePageNum - 1) * safePageSize;
        if (fromIndex >= records.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        return records.subList(fromIndex, toIndex);
    }

    /**
     * 匹配关键字
     *
     * @param itemVO 列表项
     * @param keyword 关键字
     * @return 是否匹配
     */
    private boolean matchKeyword(AdminReportListItemVO itemVO, String keyword) {
        String normalizedKeyword = keyword.trim().toLowerCase();
        String searchCorpus = String.join(" ",
                normalizeSearchField(itemVO.getId()),
                normalizeSearchField(itemVO.getUserName()),
                normalizeSearchField(itemVO.getUserEmail()),
                normalizeSearchField(itemVO.getTargetName()),
                normalizeSearchField(itemVO.getUploaderName()),
                normalizeSearchField(itemVO.getReason()));
        return searchCorpus.toLowerCase().contains(normalizedKeyword);
    }

    /**
     * 规范化搜索字段
     *
     * @param value 原值
     * @return 规范化后的字符串
     */
    private String normalizeSearchField(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * 构建网站映射
     *
     * @param websiteIds 网站ID集合
     * @return 网站映射
     */
    private Map<Long, Website> buildWebsiteMap(Set<Long> websiteIds) {
        if (websiteIds == null || websiteIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Website> websites = websiteMapper.selectBatchIds(websiteIds);
        Map<Long, Website> websiteMap = new HashMap<>();
        for (Website website : websites) {
            websiteMap.put(website.getId(), website);
        }
        return websiteMap;
    }

    /**
     * 构建评论映射
     *
     * @param commentIds 评论ID集合
     * @return 评论映射
     */
    private Map<Long, Comment> buildCommentMap(Set<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Comment> comments = commentMapper.selectBatchIds(commentIds);
        Map<Long, Comment> commentMap = new HashMap<>();
        for (Comment comment : comments) {
            commentMap.put(comment.getId(), comment);
        }
        return commentMap;
    }

    /**
     * 构建用户映射
     *
     * @param userIds 用户ID集合
     * @return 用户映射
     */
    private Map<Long, UserAccount> buildUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserAccount> users = userAccountMapper.selectBatchIds(userIds);
        Map<Long, UserAccount> userMap = new HashMap<>();
        for (UserAccount user : users) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    /**
     * 构建管理员映射
     *
     * @param adminIds 管理员ID集合
     * @return 管理员映射
     */
    private Map<Long, AdminAccount> buildAdminMap(Set<Long> adminIds) {
        if (adminIds == null || adminIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<AdminAccount> admins = adminAccountMapper.selectBatchIds(adminIds);
        Map<Long, AdminAccount> adminMap = new HashMap<>();
        for (AdminAccount admin : admins) {
            adminMap.put(admin.getId(), admin);
        }
        return adminMap;
    }

    /**
     * 解析时间字符串
     *
     * @param time 时间字符串
     * @return 时间对象
     */
    private LocalDateTime parseTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        try {
            return LocalDateTime.parse(time.trim(), DATE_TIME_FORMATTER);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "时间格式不正确，需为 yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * 计算总页数
     *
     * @param total 总数
     * @param pageSize 每页大小
     * @return 总页数
     */
    private long calcTotalPages(long total, int pageSize) {
        if (pageSize <= 0) {
            return 0L;
        }
        return total == 0 ? 0L : (total + pageSize - 1) / pageSize;
    }

    /**
     * 构建评论摘要
     *
     * @param content 评论内容
     * @return 评论摘要
     */
    private String buildCommentSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "内容待补充";
        }
        String trimmed = content.trim();
        return trimmed.length() <= 20 ? trimmed : trimmed.substring(0, 20);
    }

    /**
     * 解析用户名称
     *
     * @param user 用户对象
     * @return 用户名称
     */
    private String resolveUserName(UserAccount user) {
        return user == null ? null : user.getUsername();
    }

    /**
     * 构建举报类型标签
     *
     * @param type 举报类型
     * @return 标签
     */
    private String buildReportTypeLabel(Integer type) {
        ReportType reportType = ReportType.fromCode(type);
        if (reportType == null) {
            return "举报";
        }
        return reportType.getDescription() + "举报";
    }

    /**
     * 统计指定状态数量
     *
     * @param reportList 举报列表
     * @param status 状态值
     * @return 数量
     */
    private int countByStatus(List<Report> reportList, Integer status) {
        return (int) reportList.stream()
                .filter(report -> Objects.equals(report.getStatus(), status))
                .count();
    }

    /**
     * 统计指定类型数量
     *
     * @param reportList 举报列表
     * @param type 类型值
     * @return 数量
     */
    private int countByType(List<Report> reportList, Integer type) {
        return (int) reportList.stream()
                .filter(report -> Objects.equals(report.getType(), type))
                .count();
    }

    /**
     * 格式化时间
     *
     * @param time 时间
     * @return 时间字符串
     */
    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME_FORMATTER);
    }

    /**
     * 目标信息载体
     *
     * @param targetName 目标名称
     * @param targetUrl 目标地址
     * @param uploaderId 发布人ID
     */
    private record TargetContext(String targetName, String targetUrl, Long uploaderId, String targetStatusLabel) {
    }
}
