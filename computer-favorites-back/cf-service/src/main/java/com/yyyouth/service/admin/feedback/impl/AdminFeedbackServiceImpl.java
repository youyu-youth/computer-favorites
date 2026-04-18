package com.yyyouth.service.admin.feedback.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminFeedbackQueryDTO;
import com.yyyouth.model.dto.admin.AdminFeedbackReplyDTO;
import com.yyyouth.model.enums.FeedbackStatus;
import com.yyyouth.model.enums.FeedbackType;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.system.Feedback;
import com.yyyouth.model.vo.admin.AdminFeedbackDetailVO;
import com.yyyouth.model.vo.admin.AdminFeedbackHandleResultVO;
import com.yyyouth.model.vo.admin.AdminFeedbackListItemVO;
import com.yyyouth.model.vo.admin.AdminFeedbackPageVO;
import com.yyyouth.model.vo.admin.AdminFeedbackStatisticsVO;
import com.yyyouth.model.vo.admin.AdminFeedbackTimelineItemVO;
import com.yyyouth.service.admin.feedback.AdminFeedbackService;
import com.yyyouth.service.mapper.system.FeedbackMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * @date 2026-04-18
 *
 * 管理端反馈处理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFeedbackServiceImpl implements AdminFeedbackService {

    private static final String GUEST_USER_NAME = "游客用户";

    private static final String TIMELINE_DONE = "done";

    private static final String TIMELINE_PENDING = "pending";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FeedbackMapper feedbackMapper;

    private final UserAccountMapper userAccountMapper;

    /**
     * 查询反馈分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @Override
    public AdminFeedbackPageVO queryFeedbackPage(AdminFeedbackQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 6 : queryDTO.getPageSize();

        LambdaQueryWrapper<Feedback> wrapper = buildFeedbackQueryWrapper(queryDTO);
        wrapper.orderByAsc(Feedback::getStatus)
                .orderByDesc(Feedback::getCreateTime);

        List<Feedback> feedbackList = feedbackMapper.selectList(wrapper);
        List<AdminFeedbackListItemVO> records = enrichFeedbackList(feedbackList);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            records = records.stream()
                    .filter(item -> matchKeyword(item, queryDTO.getKeyword()))
                    .toList();
        }

        long total = records.size();
        AdminFeedbackPageVO pageVO = new AdminFeedbackPageVO();
        pageVO.setRecords(paginateRecords(records, pageNum, pageSize));
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));
        pageVO.setStatistics(buildStatistics(records));
        return pageVO;
    }

    /**
     * 查询反馈详情
     *
     * @param feedbackId 反馈ID
     * @return 反馈详情
     */
    @Override
    public AdminFeedbackDetailVO queryFeedbackDetail(Long feedbackId) {
        Feedback feedback = getRequiredFeedback(feedbackId);
        UserAccount userAccount = feedback.getUserId() == null ? null : userAccountMapper.selectById(feedback.getUserId());

        AdminFeedbackDetailVO detailVO = new AdminFeedbackDetailVO();
        fillCommonFields(detailVO, feedback, userAccount);
        detailVO.setReply(feedback.getReply());
        detailVO.setReplyTime(feedback.getReplyTime());
        detailVO.setSummaryText(buildSummaryText(feedback));
        detailVO.setTimeline(buildTimeline(feedback, userAccount));
        return detailVO;
    }

    /**
     * 回复反馈
     *
     * @param feedbackId 反馈ID
     * @param replyDTO 回复参数
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminFeedbackHandleResultVO replyFeedback(Long feedbackId, AdminFeedbackReplyDTO replyDTO) {
        Feedback feedback = getRequiredFeedback(feedbackId);
        if (Objects.equals(feedback.getStatus(), FeedbackStatus.CLOSED.getCode())) {
            throw new BusinessException(40402, "已关闭反馈不支持继续回复");
        }

        LocalDateTime now = LocalDateTime.now();
        String normalizedReply = replyDTO.getReply().trim();
        Feedback updateEntity = Feedback.builder()
                .id(feedbackId)
                .reply(normalizedReply)
                .replyTime(now)
                .status(FeedbackStatus.PROCESSED.getCode())
                .updateTime(now)
                .build();
        LambdaUpdateWrapper<Feedback> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Feedback::getId, feedbackId)
                .ne(Feedback::getStatus, FeedbackStatus.CLOSED.getCode());
        int updated = feedbackMapper.update(updateEntity, updateWrapper);
        if (updated <= 0) {
            throw new BusinessException(40402, "已关闭反馈不支持继续回复");
        }

        return buildHandleResult(feedbackId, FeedbackStatus.PROCESSED.getCode(), normalizedReply, now, now);
    }

    /**
     * 关闭反馈
     *
     * @param feedbackId 反馈ID
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminFeedbackHandleResultVO closeFeedback(Long feedbackId) {
        Feedback feedback = getRequiredFeedback(feedbackId);
        if (Objects.equals(feedback.getStatus(), FeedbackStatus.CLOSED.getCode())) {
            throw new BusinessException(40403, "该反馈已关闭，请勿重复操作");
        }

        LocalDateTime now = LocalDateTime.now();
        Feedback updateEntity = Feedback.builder()
                .id(feedbackId)
                .status(FeedbackStatus.CLOSED.getCode())
                .updateTime(now)
                .build();
        LambdaUpdateWrapper<Feedback> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Feedback::getId, feedbackId)
                .ne(Feedback::getStatus, FeedbackStatus.CLOSED.getCode());
        int updated = feedbackMapper.update(updateEntity, updateWrapper);
        if (updated <= 0) {
            throw new BusinessException(40403, "该反馈已关闭，请勿重复操作");
        }

        return buildHandleResult(feedbackId, FeedbackStatus.CLOSED.getCode(), feedback.getReply(), feedback.getReplyTime(), now);
    }

    /**
     * 构建查询条件
     *
     * @param queryDTO 查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<Feedback> buildFeedbackQueryWrapper(AdminFeedbackQueryDTO queryDTO) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Feedback::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getType() != null) {
            wrapper.eq(Feedback::getType, queryDTO.getType());
        }
        if (Boolean.TRUE.equals(queryDTO.getHasImages())) {
            wrapper.and(condition -> condition.isNotNull(Feedback::getImages)
                    .ne(Feedback::getImages, ""));
        }
        if (Boolean.TRUE.equals(queryDTO.getHasContact())) {
            wrapper.isNotNull(Feedback::getContact)
                    .apply("TRIM(contact) <> ''");
        }
        return wrapper;
    }

    /**
     * 批量补齐反馈列表展示字段
     *
     * @param feedbackList 反馈列表
     * @return 展示列表
     */
    private List<AdminFeedbackListItemVO> enrichFeedbackList(List<Feedback> feedbackList) {
        if (feedbackList == null || feedbackList.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = new LinkedHashSet<>();
        for (Feedback feedback : feedbackList) {
            if (feedback.getUserId() != null) {
                userIds.add(feedback.getUserId());
            }
        }
        Map<Long, UserAccount> userMap = buildUserMap(userIds);

        List<AdminFeedbackListItemVO> list = new ArrayList<>();
        for (Feedback feedback : feedbackList) {
            AdminFeedbackListItemVO itemVO = new AdminFeedbackListItemVO();
            fillCommonFields(itemVO, feedback, userMap.get(feedback.getUserId()));
            list.add(itemVO);
        }
        return list;
    }

    /**
     * 填充列表/详情通用字段
     *
     * @param itemVO 目标对象
     * @param feedback 反馈实体
     * @param userAccount 用户信息
     */
    private void fillCommonFields(AdminFeedbackListItemVO itemVO, Feedback feedback, UserAccount userAccount) {
        itemVO.setId(feedback.getId());
        itemVO.setUserId(feedback.getUserId());
        itemVO.setUserName(resolveUserName(feedback, userAccount));
        itemVO.setUserEmail(userAccount == null ? null : userAccount.getEmail());
        itemVO.setAvatar(resolveAvatar(feedback, userAccount));
        itemVO.setContact(feedback.getContact());
        itemVO.setType(feedback.getType());
        itemVO.setContent(feedback.getContent());
        itemVO.setImages(parseImages(feedback.getImages()));
        itemVO.setStatus(feedback.getStatus());
        itemVO.setCreateTime(feedback.getCreateTime());
        itemVO.setUpdateTime(feedback.getUpdateTime());
    }

    /**
     * 构建统计摘要
     *
     * @param records 过滤后的完整列表
     * @return 统计摘要
     */
    private AdminFeedbackStatisticsVO buildStatistics(List<AdminFeedbackListItemVO> records) {
        int total = records.size();
        int pending = countByStatus(records, FeedbackStatus.PENDING.getCode());
        int processed = countByStatus(records, FeedbackStatus.PROCESSED.getCode());
        int closed = countByStatus(records, FeedbackStatus.CLOSED.getCode());
        int bugCount = countByType(records, FeedbackType.BUG.getCode());
        int withImagesCount = (int) records.stream()
                .filter(record -> record.getImages() != null && !record.getImages().isEmpty())
                .count();
        int withContactCount = (int) records.stream()
                .filter(record -> StringUtils.hasText(record.getContact()))
                .count();
        LocalDateTime last24HourMark = LocalDateTime.now().minusHours(24);
        int last24Hours = (int) records.stream()
                .filter(record -> record.getCreateTime() != null && !record.getCreateTime().isBefore(last24HourMark))
                .count();

        AdminFeedbackStatisticsVO statisticsVO = new AdminFeedbackStatisticsVO();
        statisticsVO.setTotal(total);
        statisticsVO.setPending(pending);
        statisticsVO.setProcessed(processed);
        statisticsVO.setClosed(closed);
        statisticsVO.setLast24Hours(last24Hours);
        statisticsVO.setBugCount(bugCount);
        statisticsVO.setWithImagesCount(withImagesCount);
        statisticsVO.setWithContactCount(withContactCount);
        statisticsVO.setReplyRate(total == 0 ? 0 : Math.round(((processed + closed) * 100.0F) / total));
        return statisticsVO;
    }

    /**
     * 构建详情摘要文案
     *
     * @param feedback 反馈实体
     * @return 摘要文案
     */
    private String buildSummaryText(Feedback feedback) {
        List<String> images = parseImages(feedback.getImages());
        if (Objects.equals(feedback.getStatus(), FeedbackStatus.PENDING.getCode())) {
            return images.isEmpty()
                    ? "当前工单仍待处理，建议优先结合反馈正文和联系方式判断是否需要回访。"
                    : "当前工单仍待处理，用户已补充截图，建议优先核验附件与复现场景。";
        }
        if (Objects.equals(feedback.getStatus(), FeedbackStatus.PROCESSED.getCode())) {
            return "当前工单已完成回复，可继续保留用于问题复盘或后续跟进。";
        }
        return "当前工单已关闭，仅保留原始反馈与历史回复用于归档查阅。";
    }

    /**
     * 构建反馈时间线
     *
     * @param feedback 反馈实体
     * @param userAccount 用户信息
     * @return 时间线
     */
    private List<AdminFeedbackTimelineItemVO> buildTimeline(Feedback feedback, UserAccount userAccount) {
        List<AdminFeedbackTimelineItemVO> timeline = new ArrayList<>();

        AdminFeedbackTimelineItemVO createItem = new AdminFeedbackTimelineItemVO();
        createItem.setId("created-" + feedback.getId());
        createItem.setTitle("用户提交反馈");
        createItem.setDescription(resolveUserName(feedback, userAccount) + " 提交了" + buildFeedbackTypeLabel(feedback.getType()) + "工单。");
        createItem.setTime(formatTime(feedback.getCreateTime()));
        createItem.setTone(TIMELINE_DONE);
        timeline.add(createItem);

        if (StringUtils.hasText(feedback.getReply()) && feedback.getReplyTime() != null) {
            AdminFeedbackTimelineItemVO replyItem = new AdminFeedbackTimelineItemVO();
            replyItem.setId("replied-" + feedback.getId());
            replyItem.setTitle("管理员完成回复");
            replyItem.setDescription(feedback.getReply());
            replyItem.setTime(formatTime(feedback.getReplyTime()));
            replyItem.setTone(TIMELINE_DONE);
            timeline.add(replyItem);
        }

        if (Objects.equals(feedback.getStatus(), FeedbackStatus.CLOSED.getCode())) {
            AdminFeedbackTimelineItemVO closeItem = new AdminFeedbackTimelineItemVO();
            closeItem.setId("closed-" + feedback.getId());
            closeItem.setTitle("工单已关闭");
            closeItem.setDescription("当前工单已结束跟进，如需继续反馈请重新提交新工单。");
            closeItem.setTime(formatTime(feedback.getUpdateTime()));
            closeItem.setTone(TIMELINE_DONE);
            timeline.add(closeItem);
        } else if (!Objects.equals(feedback.getStatus(), FeedbackStatus.PROCESSED.getCode())) {
            AdminFeedbackTimelineItemVO pendingItem = new AdminFeedbackTimelineItemVO();
            pendingItem.setId("pending-" + feedback.getId());
            pendingItem.setTitle("等待管理员处理");
            pendingItem.setDescription("可直接进入处理弹窗填写回复，或在确认无需继续跟进后关闭工单。");
            pendingItem.setTime("待处理");
            pendingItem.setTone(TIMELINE_PENDING);
            timeline.add(pendingItem);
        }
        return timeline;
    }

    /**
     * 构建处理结果
     *
     * @param feedbackId 反馈ID
     * @param status 最新状态
     * @param reply 回复内容
     * @param replyTime 回复时间
     * @param updateTime 更新时间
     * @return 处理结果
     */
    private AdminFeedbackHandleResultVO buildHandleResult(Long feedbackId,
                                                          Integer status,
                                                          String reply,
                                                          LocalDateTime replyTime,
                                                          LocalDateTime updateTime) {
        AdminFeedbackHandleResultVO resultVO = new AdminFeedbackHandleResultVO();
        resultVO.setFeedbackId(feedbackId);
        resultVO.setStatus(status);
        resultVO.setReply(reply);
        resultVO.setReplyTime(replyTime);
        resultVO.setUpdateTime(updateTime);
        return resultVO;
    }

    /**
     * 获取必需的反馈记录
     *
     * @param feedbackId 反馈ID
     * @return 反馈实体
     */
    private Feedback getRequiredFeedback(Long feedbackId) {
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(40401, "反馈记录不存在");
        }
        return feedback;
    }

    /**
     * 匹配关键字
     *
     * @param itemVO 列表项
     * @param keyword 关键字
     * @return 是否匹配
     */
    private boolean matchKeyword(AdminFeedbackListItemVO itemVO, String keyword) {
        String normalizedKeyword = keyword.trim().toLowerCase();
        String searchCorpus = String.join(" ",
                normalizeSearchField(itemVO.getId()),
                normalizeSearchField(itemVO.getUserName()),
                normalizeSearchField(itemVO.getUserEmail()),
                normalizeSearchField(itemVO.getContact()),
                normalizeSearchField(itemVO.getContent()));
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
     * 分页切片
     *
     * @param records 完整记录
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 当前页数据
     */
    private List<AdminFeedbackListItemVO> paginateRecords(List<AdminFeedbackListItemVO> records, int pageNum, int pageSize) {
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
     * 解析附图 JSON
     *
     * @param images 图片 JSON
     * @return 图片列表
     */
    private List<String> parseImages(String images) {
        if (!StringUtils.hasText(images)) {
            return Collections.emptyList();
        }
        return JSONUtil.toList(images, String.class);
    }

    /**
     * 解析用户名
     *
     * @param feedback 反馈实体
     * @param userAccount 用户信息
     * @return 展示用户名
     */
    private String resolveUserName(Feedback feedback, UserAccount userAccount) {
        if (userAccount != null && StringUtils.hasText(userAccount.getUsername())) {
            return userAccount.getUsername();
        }
        return feedback.getUserId() == null ? GUEST_USER_NAME : "用户#" + feedback.getUserId();
    }

    /**
     * 解析头像地址
     *
     * @param feedback 反馈实体
     * @param userAccount 用户信息
     * @return 头像地址
     */
    private String resolveAvatar(Feedback feedback, UserAccount userAccount) {
        if (StringUtils.hasText(feedback.getAvatar())) {
            return feedback.getAvatar();
        }
        if (userAccount != null && StringUtils.hasText(userAccount.getAvatar())) {
            return userAccount.getAvatar();
        }
        return "";
    }

    /**
     * 构建反馈类型标签
     *
     * @param type 类型编码
     * @return 标签
     */
    private String buildFeedbackTypeLabel(Integer type) {
        FeedbackType feedbackType = FeedbackType.fromCode(type);
        if (feedbackType == null) {
            return "反馈";
        }
        return feedbackType.getDescription();
    }

    /**
     * 统计指定状态数量
     *
     * @param records 记录列表
     * @param status 状态值
     * @return 数量
     */
    private int countByStatus(List<AdminFeedbackListItemVO> records, Integer status) {
        return (int) records.stream()
                .filter(record -> Objects.equals(record.getStatus(), status))
                .count();
    }

    /**
     * 统计指定类型数量
     *
     * @param records 记录列表
     * @param type 类型值
     * @return 数量
     */
    private int countByType(List<AdminFeedbackListItemVO> records, Integer type) {
        return (int) records.stream()
                .filter(record -> Objects.equals(record.getType(), type))
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
}
