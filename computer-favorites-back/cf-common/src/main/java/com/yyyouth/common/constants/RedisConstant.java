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

    // ========== user-15 用户主页（贡献统计） ==========

    /**
     * 用户主页看板缓存前缀
     * 格式: user:profile:dashboard:{userId}:{section}
     * 失效时机：MQ 消费端 upsert 后按 pattern 删除该用户全部 dashboard key
     */
    public static final String USER_PROFILE_DASHBOARD_PREFIX = "user:profile:dashboard:";

    /**
     * 他人主页缓存前缀
     * 格式: user:profile:public:{username}
     * 失效时机：隐私开关更新 / 用户名变更
     */
    public static final String USER_PROFILE_PUBLIC_PREFIX = "user:profile:public:";

    /**
     * 公开主页顶层收藏夹列表（user-15 公开收藏夹）
     * 格式: user:profile:public-folders:{userId}:top:{limit}:{own|visitor}
     * 失效时机：folder 写操作 / collect 写操作 / 隐私设置变更
     */
    public static final String PUBLIC_FOLDER_TOP_PREFIX = "user:profile:public-folders:";

    /**
     * 公开主页单收藏夹一层子项（对话框使用）
     * 格式: user:profile:public-folder-children:{userId}:{folderId}:{pageNum}:{pageSize}:{own|visitor}
     */
    public static final String PUBLIC_FOLDER_CHILDREN_PREFIX = "user:profile:public-folder-children:";

    /**
     * 公开主页全量公开收藏夹树（查看全部页用）
     * 格式: user:profile:public-folder-tree:{userId}:{own|visitor}
     */
    public static final String PUBLIC_FOLDER_TREE_PREFIX = "user:profile:public-folder-tree:";

    /**
     * 用户行为事件幂等去重前缀（消费端 SETNX，TTL 24h）
     * 格式: user:stats:event:{eventId}
     */
    public static final String USER_STATS_EVENT_IDEMPOTENT_PREFIX = "user:stats:event:";

    /**
     * 定时任务分布式锁前缀
     * 格式: lock:job:{jobName}
     */
    public static final String JOB_LOCK_PREFIX = "lock:job:";
}
