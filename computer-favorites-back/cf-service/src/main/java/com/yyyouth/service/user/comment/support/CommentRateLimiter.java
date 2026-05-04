package com.yyyouth.service.user.comment.support;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 评论发布限流工具
 * 同一用户对同一网站10秒内不可重复发布评论
 *
 * @author yyyouth zg
 * @date 2026-05-04
 */
public class CommentRateLimiter {

    private static final long RATE_LIMIT_MILLIS = 10_000L;

    private static final ConcurrentHashMap<String, Long> RATE_LIMIT_MAP = new ConcurrentHashMap<>();

    /**
     * 判断是否允许发布评论
     *
     * @param userId 用户ID
     * @param websiteId 网站ID
     * @return true-允许，false-限流中
     */
    public static boolean allowComment(Long userId, Long websiteId) {
        String key = buildKey(userId, websiteId);
        Long lastTime = RATE_LIMIT_MAP.get(key);
        if (lastTime != null && System.currentTimeMillis() - lastTime < RATE_LIMIT_MILLIS) {
            return false;
        }
        return true;
    }

    /**
     * 记录评论发布时间
     *
     * @param userId 用户ID
     * @param websiteId 网站ID
     */
    public static void recordComment(Long userId, Long websiteId) {
        RATE_LIMIT_MAP.put(buildKey(userId, websiteId), System.currentTimeMillis());
    }

    /**
     * 清理过期限流记录
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        RATE_LIMIT_MAP.entrySet().removeIf(entry -> now - entry.getValue() >= RATE_LIMIT_MILLIS);
    }

    private static String buildKey(Long userId, Long websiteId) {
        return userId + ":" + websiteId;
    }
}
