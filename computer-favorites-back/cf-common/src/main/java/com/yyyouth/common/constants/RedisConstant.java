package com.yyyouth.common.constants;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2025/11/15
 *                  Redis 常量
 */
public class RedisConstant {

    /**
     * 用户登录Token前缀
     */
    public static final String WEB_USER_LOGIN_TOKEN = "web:user:login:token:";

    /**
     * 动态点赞分布式锁前缀
     * 格式: sssp:feed:like:lock:{feedId}
     */
    public static final String FEED_LIKE_LOCK = "sssp:feed:like:lock:";

    /**
     * 动态评论列表缓存前缀
     * 格式: sssp:feed:comment:list:{feedId}
     */
    public static final String FEED_COMMENT_LIST = "sssp:feed:comment:list:";

    /**
     * 动态评论数量缓存前缀
     * 格式: sssp:feed:comment:count:{feedId}
     */
    public static final String FEED_COMMENT_COUNT = "sssp:feed:comment:count:";

    /**
     * 评论点赞分布式锁前缀
     * 格式: sssp:feed:comment:like:lock:{commentId}
     */
    public static final String FEED_COMMENT_LIKE_LOCK = "sssp:feed:comment:like:lock:";
}
