package com.yyyouth.service.user.comment.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.utils.SensitiveWordUtils;
import com.yyyouth.model.dto.user.CommentCreateDTO;
import com.yyyouth.model.dto.user.CommentPageQueryDTO;
import com.yyyouth.model.dto.user.MyCommentPageQueryDTO;
import com.yyyouth.model.enums.UserMessageType;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.system.SystemMessage;
import com.yyyouth.model.pojo.website.Comment;
import com.yyyouth.model.pojo.website.CommentLike;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.vo.user.CommentItemVO;
import com.yyyouth.model.vo.user.CommentPageVO;
import com.yyyouth.model.vo.user.CommentReplyVO;
import com.yyyouth.model.vo.user.CommentUserVO;
import com.yyyouth.model.vo.user.MyCommentItemVO;
import com.yyyouth.model.vo.user.MyCommentPageVO;
import com.yyyouth.service.mapper.system.SystemMessageMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.website.CommentLikeMapper;
import com.yyyouth.service.mapper.website.CommentMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.comment.UserCommentService;
import com.yyyouth.service.user.comment.support.CommentRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
 * 用户评论服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserCommentServiceImpl implements UserCommentService {

    private static final int NOT_DELETED = 0;

    private static final int DELETED = 1;

    private static final int VISIBLE_STATUS = 1;

    private static final int HIDDEN_STATUS = 0;

    private static final int ONLINE_STATUS = 1;

    private static final int AUDIT_APPROVED_STATUS = 1;

    private static final long TOP_LEVEL_PARENT_ID = 0L;

    private static final String DELETED_COMMENT_PLACEHOLDER = "该评论已删除";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CommentMapper commentMapper;

    private final CommentLikeMapper commentLikeMapper;

    private final WebsiteMapper websiteMapper;

    private final UserAccountMapper userAccountMapper;

    private final SystemMessageMapper systemMessageMapper;

    /**
     * 发布评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishComment(Long websiteId, CommentCreateDTO dto) {
        Long userId = requireLogin();

        validateWebsiteAvailable(websiteId);

        String content = normalizeContent(dto.getContent());
        validateSensitiveWord(content);

        if (!CommentRateLimiter.allowComment(userId, websiteId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "评论过于频繁，请稍后再试");
        }

        Long parentId = dto.getParentId();
        Long replyUserId = dto.getReplyUserId();

        if (parentId != null && parentId > TOP_LEVEL_PARENT_ID) {
            Comment parentComment = validateParentComment(parentId);
            if (!Objects.equals(parentComment.getParentId(), TOP_LEVEL_PARENT_ID)) {
                replyUserId = parentComment.getUserId();
                parentId = parentComment.getParentId();
            }
        } else {
            parentId = TOP_LEVEL_PARENT_ID;
        }

        String ip = getClientIp();

        Comment comment = Comment.builder()
                .userId(userId)
                .websiteId(websiteId)
                .parentId(parentId)
                .replyUserId(replyUserId)
                .content(content)
                .ip(ip)
                .likeCount(0)
                .status(VISIBLE_STATUS)
                .deleted(NOT_DELETED)
                .build();
        int rows = commentMapper.insert(comment);
        if (rows != 1 || comment.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "评论发布失败，请稍后重试");
        }

        websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                .eq(Website::getId, websiteId)
                .setSql("comment_count = comment_count + 1"));

        CommentRateLimiter.recordComment(userId, websiteId);

        if (parentId > TOP_LEVEL_PARENT_ID && replyUserId != null) {
            sendReplyNotification(replyUserId, content, comment.getId());
        }

        log.info("评论发布成功，commentId={}, userId={}, websiteId={}, parentId={}",
                comment.getId(), userId, websiteId, parentId);
        return comment.getId();
    }

    /**
     * 分页查询评论列表（两级折叠）
     */
    @Override
    public CommentPageVO pageComments(Long websiteId, CommentPageQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();
        String sort = StringUtils.hasText(queryDTO.getSort()) ? queryDTO.getSort() : "time";

        LambdaQueryWrapper<Comment> topLevelWrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getWebsiteId, websiteId)
                .eq(Comment::getParentId, TOP_LEVEL_PARENT_ID)
                .eq(Comment::getDeleted, NOT_DELETED)
                .eq(Comment::getStatus, VISIBLE_STATUS);

        if ("hot".equals(sort)) {
            topLevelWrapper.orderByDesc(Comment::getLikeCount).orderByDesc(Comment::getCreateTime);
        } else {
            topLevelWrapper.orderByDesc(Comment::getCreateTime);
        }

        Page<Comment> page = commentMapper.selectPage(new Page<>(pageNum, pageSize), topLevelWrapper);

        List<Comment> topLevelComments = page.getRecords();
        if (topLevelComments.isEmpty()) {
            return buildEmptyCommentPage(pageNum, pageSize);
        }

        List<Long> topLevelIds = topLevelComments.stream().map(Comment::getId).toList();

        List<Comment> allReplies = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getWebsiteId, websiteId)
                .in(Comment::getParentId, topLevelIds)
                .eq(Comment::getDeleted, NOT_DELETED)
                .eq(Comment::getStatus, VISIBLE_STATUS)
                .orderByAsc(Comment::getCreateTime));

        Map<Long, List<Comment>> repliesByParent = allReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        Map<Long, Long> replyCountMap = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getWebsiteId, websiteId)
                        .in(Comment::getParentId, topLevelIds)
                        .eq(Comment::getDeleted, NOT_DELETED)
                        .eq(Comment::getStatus, VISIBLE_STATUS)
                        .select(Comment::getParentId))
                .stream()
                .collect(Collectors.groupingBy(Comment::getParentId, Collectors.counting()));

        Set<Long> allUserIds = collectUserIds(topLevelComments, allReplies);
        Set<Long> allReplyUserIds = allReplies.stream()
                .map(Comment::getReplyUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        allUserIds.addAll(allReplyUserIds);
        Map<Long, CommentUserVO> userMap = batchQueryUsers(allUserIds);

        Set<Long> likedCommentIds = Collections.emptySet();
        if (StpUtil.isLogin()) {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            List<Long> allCommentIds = new ArrayList<>(topLevelIds);
            allReplies.forEach(r -> allCommentIds.add(r.getId()));
            likedCommentIds = batchQueryLikedIds(currentUserId, allCommentIds);
        }

        Set<Long> finalLikedCommentIds = likedCommentIds;
        List<CommentItemVO> itemVOs = topLevelComments.stream().map(tc -> {
            List<Comment> replies = repliesByParent.getOrDefault(tc.getId(), List.of());
            List<CommentReplyVO> replyVOs = replies.stream().map(r -> buildReplyVO(r, userMap, finalLikedCommentIds)).toList();

            return CommentItemVO.builder()
                    .id(tc.getId())
                    .user(userMap.get(tc.getUserId()))
                    .content(tc.getDeleted() == DELETED ? DELETED_COMMENT_PLACEHOLDER : tc.getContent())
                    .createTime(formatTime(tc.getCreateTime()))
                    .likeCount(tc.getLikeCount())
                    .isLiked(finalLikedCommentIds.contains(tc.getId()))
                    .isDeleted(tc.getDeleted() == DELETED)
                    .replyCount(replyCountMap.getOrDefault(tc.getId(), 0L).intValue())
                    .replies(replyVOs)
                    .build();
        }).toList();

        return CommentPageVO.builder()
                .records(itemVOs)
                .total(page.getTotal())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages((int) Math.ceil((double) page.getTotal() / pageSize))
                .build();
    }

    /**
     * 删除评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long userId = requireLogin();

        Comment comment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, commentId)
                .last("limit 1"));
        if (comment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在");
        }
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权删除该评论");
        }
        if (comment.getDeleted() == DELETED) {
            return;
        }

        boolean hasReplies = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, commentId)
                .eq(Comment::getDeleted, NOT_DELETED)) > 0;

        if (hasReplies) {
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, commentId)
                    .set(Comment::getContent, DELETED_COMMENT_PLACEHOLDER)
                    .set(Comment::getDeleted, DELETED));
        } else {
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, commentId)
                    .set(Comment::getDeleted, DELETED));
        }

        websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                .eq(Website::getId, comment.getWebsiteId())
                .gt(Website::getCommentCount, 0)
                .setSql("comment_count = comment_count - 1"));

        log.info("评论删除成功，commentId={}, userId={}, hasReplies={}", commentId, userId, hasReplies);
    }

    /**
     * 点赞评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId) {
        Long userId = requireLogin();

        validateCommentExists(commentId);

        CommentLike like = CommentLike.builder()
                .userId(userId)
                .commentId(commentId)
                .createTime(LocalDateTime.now())
                .build();
        try {
            int rows = commentLikeMapper.insert(like);
            if (rows == 1) {
                commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                        .eq(Comment::getId, commentId)
                        .setSql("like_count = like_count + 1"));
            }
        } catch (DuplicateKeyException e) {
            log.debug("重复点赞，userId={}, commentId={}，幂等处理", userId, commentId);
        }
    }

    /**
     * 取消点赞评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeComment(Long commentId) {
        Long userId = requireLogin();

        int rows = commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, userId)
                .eq(CommentLike::getCommentId, commentId));
        if (rows > 0) {
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, commentId)
                    .gt(Comment::getLikeCount, 0)
                    .setSql("like_count = like_count - 1"));
        }
    }

    /**
     * 分页查询我的评论
     */
    @Override
    public MyCommentPageVO pageMyComments(MyCommentPageQueryDTO queryDTO) {
        Long userId = requireLogin();
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();

        Page<Comment> page = commentMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, userId)
                        .orderByDesc(Comment::getCreateTime));

        List<Comment> records = page.getRecords();
        if (records.isEmpty()) {
            return MyCommentPageVO.builder()
                    .records(List.of())
                    .total(0L)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .totalPages(0)
                    .build();
        }

        Set<Long> websiteIds = records.stream().map(Comment::getWebsiteId).collect(Collectors.toSet());
        Map<Long, Website> websiteMap = websiteMapper.selectList(new LambdaQueryWrapper<Website>()
                        .in(Website::getId, websiteIds))
                .stream().collect(Collectors.toMap(Website::getId, w -> w));

        List<MyCommentItemVO> itemVOs = records.stream().map(c -> {
            Website w = websiteMap.get(c.getWebsiteId());
            return MyCommentItemVO.builder()
                    .id(c.getId())
                    .websiteId(c.getWebsiteId())
                    .websiteName(w != null ? w.getName() : "未知网站")
                    .websiteIcon(w != null ? w.getIcon() : null)
                    .content(c.getDeleted() == DELETED ? DELETED_COMMENT_PLACEHOLDER : c.getContent())
                    .createTime(formatTime(c.getCreateTime()))
                    .likeCount(c.getLikeCount())
                    .parentId(c.getParentId())
                    .isDeleted(c.getDeleted() == DELETED)
                    .build();
        }).toList();

        return MyCommentPageVO.builder()
                .records(itemVOs)
                .total(page.getTotal())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages((int) Math.ceil((double) page.getTotal() / pageSize))
                .build();
    }

    private Long requireLogin() {
        StpUtil.checkLogin();
        return StpUtil.getLoginIdAsLong();
    }

    private void validateWebsiteAvailable(Long websiteId) {
        Website website = websiteMapper.selectOne(new LambdaQueryWrapper<Website>()
                .eq(Website::getId, websiteId)
                .eq(Website::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (website == null || !Objects.equals(website.getStatus(), ONLINE_STATUS)
                || !Objects.equals(website.getAuditStatus(), AUDIT_APPROVED_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "网站不存在或已下架");
        }
    }

    private Comment validateParentComment(Long parentId) {
        Comment parent = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, parentId)
                .last("limit 1"));
        if (parent == null || parent.getDeleted() == DELETED || Objects.equals(parent.getStatus(), HIDDEN_STATUS)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "回复的评论不存在或已被删除");
        }
        return parent;
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "评论内容不能为空");
        }
        return content.trim();
    }

    private void validateSensitiveWord(String content) {
        if (SensitiveWordUtils.containsForUserContent(content)) {
            String word = SensitiveWordUtils.findFirst(content);
            String msg = StringUtils.hasText(word)
                    ? "评论包含敏感词，命中词：" + word
                    : "评论包含敏感词";
            throw new BusinessException(HttpStatus.BAD_REQUEST, msg);
        }
    }

    private void validateCommentExists(Long commentId) {
        Comment comment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, commentId)
                .last("limit 1"));
        if (comment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在");
        }
    }

    private void sendReplyNotification(Long replyUserId, String content, Long commentId) {
        if (Objects.equals(replyUserId, StpUtil.getLoginIdAsLong())) {
            return;
        }
        SystemMessage message = SystemMessage.builder()
                .userId(replyUserId)
                .title("收到评论回复")
                .content(content.length() > 50 ? content.substring(0, 50) + "..." : content)
                .type(UserMessageType.COMMENT_REPLY.getCode())
                .relatedId(commentId)
                .isRead(0)
                .build();
        systemMessageMapper.insert(message);
    }

    private Set<Long> collectUserIds(List<Comment> topLevel, List<Comment> replies) {
        Set<Long> ids = topLevel.stream().map(Comment::getUserId).collect(Collectors.toSet());
        replies.forEach(r -> ids.add(r.getUserId()));
        return ids;
    }

    private Map<Long, CommentUserVO> batchQueryUsers(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
                        .in(UserAccount::getId, userIds)
                        .select(UserAccount::getId, UserAccount::getNickname, UserAccount::getAvatar))
                .stream()
                .collect(Collectors.toMap(
                        UserAccount::getId,
                        u -> CommentUserVO.builder()
                                .id(u.getId())
                                .nickname(u.getNickname())
                                .avatar(u.getAvatar())
                                .build()));
    }

    private Set<Long> batchQueryLikedIds(Long userId, List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        return commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getUserId, userId)
                        .in(CommentLike::getCommentId, commentIds)
                        .select(CommentLike::getCommentId))
                .stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toSet());
    }

    private CommentReplyVO buildReplyVO(Comment reply, Map<Long, CommentUserVO> userMap, Set<Long> likedIds) {
        String replyTo = null;
        if (reply.getReplyUserId() != null) {
            CommentUserVO replyUser = userMap.get(reply.getReplyUserId());
            if (replyUser != null) {
                replyTo = replyUser.getNickname();
            }
        }
        return CommentReplyVO.builder()
                .id(reply.getId())
                .user(userMap.get(reply.getUserId()))
                .content(reply.getDeleted() == DELETED ? DELETED_COMMENT_PLACEHOLDER : reply.getContent())
                .createTime(formatTime(reply.getCreateTime()))
                .likeCount(reply.getLikeCount())
                .isLiked(likedIds.contains(reply.getId()))
                .isDeleted(reply.getDeleted() == DELETED)
                .replyTo(replyTo)
                .build();
    }

    private CommentPageVO buildEmptyCommentPage(int pageNum, int pageSize) {
        return CommentPageVO.builder()
                .records(List.of())
                .total(0L)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages(0)
                .build();
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                int idx = ip.indexOf(',');
                return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
            }
            ip = request.getHeader("X-Real-IP");
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip.trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
