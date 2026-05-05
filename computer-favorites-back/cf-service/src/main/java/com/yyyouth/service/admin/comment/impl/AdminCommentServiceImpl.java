package com.yyyouth.service.admin.comment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminCommentBatchStatusUpdateDTO;
import com.yyyouth.model.dto.admin.AdminCommentQueryDTO;
import com.yyyouth.model.dto.admin.AdminCommentStatusUpdateDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.website.Comment;
import com.yyyouth.model.pojo.website.CommentReport;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.vo.admin.AdminCommentBatchStatusResultVO;
import com.yyyouth.model.vo.admin.AdminCommentDetailVO;
import com.yyyouth.model.vo.admin.AdminCommentListItemVO;
import com.yyyouth.model.vo.admin.AdminCommentPageVO;
import com.yyyouth.model.vo.admin.AdminCommentReplyItemVO;
import com.yyyouth.model.vo.admin.AdminCommentStatisticsVO;
import com.yyyouth.service.admin.comment.AdminCommentService;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.website.CommentMapper;
import com.yyyouth.service.mapper.website.CommentReportMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论管理服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {

    private static final int NOT_DELETED = 0;

    private static final int DELETED = 1;

    private static final int VISIBLE_STATUS = 1;

    private static final int HIDDEN_STATUS = 0;

    private static final long TOP_LEVEL_PARENT_ID = 0L;

    private static final int PENDING_REPORT_STATUS = 0;

    private final CommentMapper commentMapper;

    private final WebsiteMapper websiteMapper;

    private final UserAccountMapper userAccountMapper;

    private final CommentReportMapper commentReportMapper;

    /**
     * 分页查询评论列表
     */
    @Override
    public AdminCommentPageVO queryCommentPage(AdminCommentQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDeleted, NOT_DELETED)
                .eq(Comment::getParentId, TOP_LEVEL_PARENT_ID);

        if (queryDTO.getStatus() != null) {
            wrapper.eq(Comment::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(Comment::getCreateTime);

        List<Comment> allComments = commentMapper.selectList(wrapper);

        List<AdminCommentListItemVO> allItems = enrichCommentList(allComments);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim().toLowerCase();
            allItems = allItems.stream()
                    .filter(item -> matchKeyword(item, keyword))
                    .toList();
        }

        long total = allItems.size();
        long totalPages = Math.max(1, (total + pageSize - 1) / pageSize);
        int safePageNum = Math.min(pageNum, (int) totalPages);
        int fromIndex = (safePageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, allItems.size());

        List<AdminCommentListItemVO> pagedRecords = fromIndex < allItems.size()
                ? allItems.subList(fromIndex, toIndex)
                : List.of();

        AdminCommentPageVO pageVO = new AdminCommentPageVO();
        pageVO.setRecords(pagedRecords);
        pageVO.setTotal(total);
        pageVO.setPageNum(safePageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(totalPages);
        return pageVO;
    }

    /**
     * 查询评论统计概览
     */
    @Override
    public AdminCommentStatisticsVO queryCommentStatistics() {
        LambdaQueryWrapper<Comment> baseWrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDeleted, NOT_DELETED);

        long total = commentMapper.selectCount(baseWrapper);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayNew = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDeleted, NOT_DELETED)
                .ge(Comment::getCreateTime, todayStart));

        long visible = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDeleted, NOT_DELETED)
                .eq(Comment::getStatus, VISIBLE_STATUS));

        long hidden = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDeleted, NOT_DELETED)
                .eq(Comment::getStatus, HIDDEN_STATUS));

        AdminCommentStatisticsVO vo = new AdminCommentStatisticsVO();
        vo.setTotal((int) total);
        vo.setTodayNew((int) todayNew);
        vo.setVisible((int) visible);
        vo.setHidden((int) hidden);
        return vo;
    }

    /**
     * 查询评论详情（含回复列表）
     */
    @Override
    public AdminCommentDetailVO queryCommentDetail(Long commentId) {
        Comment comment = requireComment(commentId);

        AdminCommentDetailVO detailVO = new AdminCommentDetailVO();
        fillBaseListItemFields(detailVO, comment);

        Set<Long> userIds = Set.of(comment.getUserId());
        Map<Long, UserAccount> userMap = batchQueryUserAccounts(userIds);
        UserAccount user = userMap.get(comment.getUserId());
        if (user != null) {
            detailVO.setUserName(user.getNickname());
            detailVO.setUserAvatar(user.getAvatar());
        }

        Website website = websiteMapper.selectOne(new LambdaQueryWrapper<Website>()
                .eq(Website::getId, comment.getWebsiteId())
                .select(Website::getId, Website::getName, Website::getIcon)
                .last("limit 1"));
        if (website != null) {
            detailVO.setWebsiteName(website.getName());
            detailVO.setWebsiteIcon(website.getIcon());
        }

        List<Comment> replies = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, commentId)
                .eq(Comment::getDeleted, NOT_DELETED)
                .orderByAsc(Comment::getCreateTime));

        List<AdminCommentReplyItemVO> replyVOs = buildReplyList(replies);
        detailVO.setReplies(replyVOs);

        if (Objects.equals(comment.getStatus(), HIDDEN_STATUS)) {
            detailVO.setLastAdminAction("隐藏评论");
            detailVO.setLastAdminActionTime(comment.getUpdateTime());
        } else {
            detailVO.setLastAdminAction(null);
            detailVO.setLastAdminActionTime(null);
        }

        return detailVO;
    }

    /**
     * 单条评论状态变更
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentStatus(Long commentId, AdminCommentStatusUpdateDTO statusDTO) {
        Comment comment = requireComment(commentId);

        int targetStatus = statusDTO.getStatus();
        if (Objects.equals(comment.getStatus(), targetStatus)) {
            log.info("评论状态已是目标状态，幂等处理，commentId={}, status={}", commentId, targetStatus);
            return;
        }

        int oldStatus = comment.getStatus();
        commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                .eq(Comment::getId, commentId)
                .set(Comment::getStatus, targetStatus));

        syncWebsiteCommentCount(comment, oldStatus, targetStatus);

        log.info("管理端变更评论状态，commentId={}, {} -> {}, reason={}",
                commentId, oldStatus, targetStatus, statusDTO.getReason());
    }

    /**
     * 批量评论状态变更
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminCommentBatchStatusResultVO batchUpdateCommentStatus(AdminCommentBatchStatusUpdateDTO batchDTO) {
        int total = batchDTO.getIds().size();
        int success = 0;
        int skipped = 0;
        int targetStatus = batchDTO.getStatus();

        for (Long id : batchDTO.getIds()) {
            Comment comment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getId, id)
                    .last("limit 1"));

            if (comment == null || Objects.equals(comment.getDeleted(), DELETED)) {
                skipped++;
                continue;
            }

            if (Objects.equals(comment.getStatus(), targetStatus)) {
                skipped++;
                continue;
            }

            int oldStatus = comment.getStatus();
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, id)
                    .set(Comment::getStatus, targetStatus));

            syncWebsiteCommentCount(comment, oldStatus, targetStatus);
            success++;
        }

        String actionText = targetStatus == HIDDEN_STATUS ? "隐藏" : "显示";
        String message = String.format("批量%s完成：成功%d条，跳过%d条", actionText, success, skipped);

        AdminCommentBatchStatusResultVO resultVO = new AdminCommentBatchStatusResultVO();
        resultVO.setTotal(total);
        resultVO.setSuccess(success);
        resultVO.setSkipped(skipped);
        resultVO.setMessage(message);
        return resultVO;
    }

    private Comment requireComment(Long commentId) {
        Comment comment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, commentId)
                .last("limit 1"));
        if (comment == null || Objects.equals(comment.getDeleted(), DELETED)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在");
        }
        return comment;
    }

    private void syncWebsiteCommentCount(Comment comment, int oldStatus, int newStatus) {
        if (!Objects.equals(comment.getParentId(), TOP_LEVEL_PARENT_ID)) {
            return;
        }

        if (oldStatus == VISIBLE_STATUS && newStatus == HIDDEN_STATUS) {
            websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                    .eq(Website::getId, comment.getWebsiteId())
                    .gt(Website::getCommentCount, 0)
                    .setSql("comment_count = comment_count - 1"));
        } else if (oldStatus == HIDDEN_STATUS && newStatus == VISIBLE_STATUS) {
            websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                    .eq(Website::getId, comment.getWebsiteId())
                    .setSql("comment_count = comment_count + 1"));
        }
    }

    private List<AdminCommentListItemVO> enrichCommentList(List<Comment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }

        Set<Long> websiteIds = comments.stream().map(Comment::getWebsiteId).collect(Collectors.toSet());
        Map<Long, Website> websiteMap = websiteMapper.selectList(new LambdaQueryWrapper<Website>()
                        .in(Website::getId, websiteIds))
                .stream().collect(Collectors.toMap(Website::getId, w -> w));

        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, UserAccount> userMap = batchQueryUserAccounts(userIds);

        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        Map<Long, Long> replyCountMap = batchCountReplies(commentIds);
        Map<Long, Long> reportCountMap = batchCountPendingReports(commentIds);

        return comments.stream().map(c -> {
            AdminCommentListItemVO item = new AdminCommentListItemVO();
            fillBaseListItemFields(item, c);

            UserAccount user = userMap.get(c.getUserId());
            if (user != null) {
                item.setUserName(user.getNickname());
                item.setUserAvatar(user.getAvatar());
            }

            Website w = websiteMap.get(c.getWebsiteId());
            if (w != null) {
                item.setWebsiteName(w.getName());
                item.setWebsiteIcon(w.getIcon());
            }

            item.setReplyCount(replyCountMap.getOrDefault(c.getId(), 0L).intValue());
            item.setReportCount(reportCountMap.getOrDefault(c.getId(), 0L).intValue());
            return item;
        }).toList();
    }

    private void fillBaseListItemFields(AdminCommentListItemVO item, Comment c) {
        item.setId(c.getId());
        item.setWebsiteId(c.getWebsiteId());
        item.setUserId(c.getUserId());
        item.setContent(c.getContent());
        item.setLikeCount(c.getLikeCount());
        item.setStatus(c.getStatus());
        item.setCreateTime(c.getCreateTime());
        item.setUpdateTime(c.getUpdateTime());
    }

    private List<AdminCommentReplyItemVO> buildReplyList(List<Comment> replies) {
        if (replies.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = replies.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Set<Long> replyUserIds = replies.stream()
                .map(Comment::getReplyUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        userIds.addAll(replyUserIds);
        Map<Long, UserAccount> userMap = batchQueryUserAccounts(userIds);

        return replies.stream().map(r -> {
            AdminCommentReplyItemVO vo = new AdminCommentReplyItemVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setContent(r.getContent());
            vo.setLikeCount(r.getLikeCount());
            vo.setIsDeleted(false);
            vo.setCreateTime(r.getCreateTime());

            UserAccount user = userMap.get(r.getUserId());
            if (user != null) {
                vo.setUserName(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            }

            if (r.getReplyUserId() != null) {
                UserAccount replyUser = userMap.get(r.getReplyUserId());
                if (replyUser != null) {
                    vo.setReplyTo(replyUser.getNickname());
                }
            }

            return vo;
        }).toList();
    }

    private Map<Long, UserAccount> batchQueryUserAccounts(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
                        .in(UserAccount::getId, userIds)
                        .select(UserAccount::getId, UserAccount::getNickname, UserAccount::getAvatar))
                .stream()
                .collect(Collectors.toMap(UserAccount::getId, u -> u));
    }

    private Map<Long, Long> batchCountReplies(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                        .in(Comment::getParentId, parentIds)
                        .eq(Comment::getDeleted, NOT_DELETED)
                        .select(Comment::getParentId))
                .stream()
                .collect(Collectors.groupingBy(Comment::getParentId, Collectors.counting()));
    }

    private Map<Long, Long> batchCountPendingReports(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentReportMapper.selectList(new LambdaQueryWrapper<CommentReport>()
                        .in(CommentReport::getCommentId, commentIds)
                        .eq(CommentReport::getStatus, PENDING_REPORT_STATUS)
                        .select(CommentReport::getCommentId))
                .stream()
                .collect(Collectors.groupingBy(CommentReport::getCommentId, Collectors.counting()));
    }

    private boolean matchKeyword(AdminCommentListItemVO item, String keyword) {
        if (item.getUserName() != null && item.getUserName().toLowerCase().contains(keyword)) {
            return true;
        }
        return item.getContent() != null && item.getContent().toLowerCase().contains(keyword);
    }
}
