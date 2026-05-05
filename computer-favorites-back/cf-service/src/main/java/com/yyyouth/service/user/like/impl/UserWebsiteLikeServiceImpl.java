package com.yyyouth.service.user.like.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.WebsiteLikePageDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteLike;
import com.yyyouth.model.vo.user.UserWebsiteTagItemVO;
import com.yyyouth.model.vo.user.WebsiteLikeItemVO;
import com.yyyouth.model.vo.user.WebsiteLikePageVO;
import com.yyyouth.service.mapper.website.WebsiteLikeMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.like.UserWebsiteLikeService;
import com.yyyouth.service.user.website.support.UserWebsiteTagSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 用户网站点赞服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserWebsiteLikeServiceImpl implements UserWebsiteLikeService {

    private static final int NOT_DELETED = 0;

    private static final int ONLINE_STATUS = 1;

    private static final int AUDIT_APPROVED_STATUS = 1;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WebsiteLikeMapper websiteLikeMapper;

    private final WebsiteMapper websiteMapper;

    private final UserWebsiteTagSupport userWebsiteTagSupport;

    /**
     * 网站点赞
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long websiteId) {
        Long userId = StpUtil.getLoginIdAsLong();

        validateWebsiteOnline(websiteId);

        WebsiteLike like = WebsiteLike.builder()
                .userId(userId)
                .websiteId(websiteId)
                .createTime(LocalDateTime.now())
                .build();
        try {
            int rows = websiteLikeMapper.insert(like);
            if (rows == 1) {
                websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                        .eq(Website::getId, websiteId)
                        .setSql("like_count = like_count + 1"));
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.debug("重复点赞，userId={}, websiteId={}，幂等处理", userId, websiteId);
        }
    }

    /**
     * 取消点赞
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long websiteId) {
        Long userId = StpUtil.getLoginIdAsLong();

        int rows = websiteLikeMapper.delete(new LambdaQueryWrapper<WebsiteLike>()
                .eq(WebsiteLike::getUserId, userId)
                .eq(WebsiteLike::getWebsiteId, websiteId));
        if (rows > 0) {
            websiteMapper.update(null, new LambdaUpdateWrapper<Website>()
                    .eq(Website::getId, websiteId)
                    .gt(Website::getLikeCount, 0)
                    .setSql("like_count = like_count - 1"));
        }
    }

    /**
     * 查询当前用户是否已点赞
     */
    @Override
    public Boolean isLiked(Long websiteId) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Long count = websiteLikeMapper.selectCount(new LambdaQueryWrapper<WebsiteLike>()
                .eq(WebsiteLike::getUserId, userId)
                .eq(WebsiteLike::getWebsiteId, websiteId));
        return count != null && count > 0;
    }

    /**
     * 我点赞的网站列表（分页）
     */
    @Override
    public WebsiteLikePageVO pageMyLikes(WebsiteLikePageDTO pageDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        int pageNum = pageDTO.getPageNum();
        int pageSize = pageDTO.getPageSize();

        long total = websiteLikeMapper.selectCount(new LambdaQueryWrapper<WebsiteLike>()
                .eq(WebsiteLike::getUserId, userId));

        int totalPages = (int) ((total + pageSize - 1) / pageSize);

        if (total == 0) {
            return WebsiteLikePageVO.builder()
                    .records(Collections.emptyList())
                    .total(0L)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .totalPages(0)
                    .build();
        }

        int offset = (pageNum - 1) * pageSize;
        List<WebsiteLike> likeRecords = websiteLikeMapper.selectList(new LambdaQueryWrapper<WebsiteLike>()
                .eq(WebsiteLike::getUserId, userId)
                .orderByDesc(WebsiteLike::getCreateTime)
                .last("limit " + offset + "," + pageSize));

        List<Long> websiteIds = likeRecords.stream()
                .map(WebsiteLike::getWebsiteId)
                .distinct()
                .toList();

        Map<Long, Website> websiteMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(websiteIds)) {
            List<Website> websites = websiteMapper.selectBatchIds(websiteIds);
            websiteMap = websites.stream()
                    .filter(w -> w.getDeleted() == NOT_DELETED)
                    .collect(Collectors.toMap(Website::getId, w -> w, (a, b) -> a));
        }

        Set<Long> tagIds = websiteMap.values().stream()
                .flatMap(w -> userWebsiteTagSupport.parseTagIds(w.getTags()).stream())
                .collect(Collectors.toSet());
        Map<Long, UserWebsiteTagItemVO> tagItemMap = userWebsiteTagSupport.buildTagItemMap(tagIds);

        Map<Long, Website> finalWebsiteMap = websiteMap;
        List<WebsiteLikeItemVO> itemVOS = likeRecords.stream()
                .map(like -> {
                    Website website = finalWebsiteMap.get(like.getWebsiteId());
                    if (website == null) {
                        return null;
                    }
                    return WebsiteLikeItemVO.builder()
                            .id(like.getId())
                            .websiteId(website.getId())
                            .websiteName(website.getName())
                            .websiteUrl(website.getUrl())
                            .websiteIcon(website.getIcon())
                            .websiteSummary(website.getSummary())
                            .websiteTags(userWebsiteTagSupport.buildWebsiteTagItems(website.getTags(), tagItemMap))
                            .likeCount(website.getLikeCount())
                            .score(website.getScore())
                            .likeTime(like.getCreateTime() != null ? like.getCreateTime().format(TIME_FORMATTER) : "")
                            .build();
                })
                .filter(item -> item != null)
                .toList();

        return WebsiteLikePageVO.builder()
                .records(itemVOS)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();
    }

    /**
     * 校验网站是否在线
     */
    private void validateWebsiteOnline(Long websiteId) {
        Long count = websiteMapper.selectCount(new LambdaQueryWrapper<Website>()
                .eq(Website::getId, websiteId)
                .eq(Website::getDeleted, NOT_DELETED)
                .eq(Website::getStatus, ONLINE_STATUS)
                .eq(Website::getAuditStatus, AUDIT_APPROVED_STATUS));
        if (count == null || count == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "网站不存在或已下架");
        }
    }
}
