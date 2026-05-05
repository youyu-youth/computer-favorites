package com.yyyouth.service.user.score.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteScore;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.mapper.website.WebsiteScoreMapper;
import com.yyyouth.service.user.score.UserWebsiteScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 用户网站评分服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserWebsiteScoreServiceImpl implements UserWebsiteScoreService {

    private static final int NOT_DELETED = 0;

    private static final int ONLINE_STATUS = 1;

    private static final int AUDIT_APPROVED_STATUS = 1;

    private final WebsiteScoreMapper websiteScoreMapper;

    private final WebsiteMapper websiteMapper;

    /**
     * 网站评分（upsert）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void score(Long websiteId, Integer score) {
        Long userId = StpUtil.getLoginIdAsLong();

        validateWebsiteOnline(websiteId);

        websiteScoreMapper.upsert(userId, websiteId, score);
        websiteMapper.recalculateScore(websiteId);

        log.info("网站评分成功，userId={}, websiteId={}, score={}", userId, websiteId, score);
    }

    /**
     * 查询我的评分
     */
    @Override
    public Integer getMyScore(Long websiteId) {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        WebsiteScore websiteScore = websiteScoreMapper.selectOne(new LambdaQueryWrapper<WebsiteScore>()
                .eq(WebsiteScore::getUserId, userId)
                .eq(WebsiteScore::getWebsiteId, websiteId)
                .last("limit 1"));
        return websiteScore != null ? websiteScore.getScore() : null;
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
