package com.yyyouth.service.user.userstats;

import com.yyyouth.model.dto.userstats.UserActivityEvent;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户贡献统计聚合服务 - user-15 用户主页
 * 由 UserStatsConsumer 消费 RabbitMQ 事件后调用，负责增量 upsert 三张快照表 + 失效缓存。
 */
public interface UserStatsAggregateService {

    /**
     * 处理一条用户行为事件，更新 t_user_stats_daily / t_user_stats_overview / t_user_tag_affinity。
     *
     * @param event 用户行为事件
     * @return true=成功处理；false=幂等去重命中（重复事件，已被前序成功消费）
     */
    boolean apply(UserActivityEvent event);
}
