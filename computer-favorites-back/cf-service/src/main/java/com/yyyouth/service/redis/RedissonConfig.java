package com.yyyouth.service.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * Redisson 单节点客户端配置。
 * 复用 spring.data.redis.* 的连接参数；未显式声明 cluster/sentinel 时一律按单机构建。
 * 若项目其他位置已声明 {@link RedissonClient}，本配置自动失效（@ConditionalOnMissingBean）。
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${spring.data.redis.timeout:3000ms}")
    private String timeoutStr;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig single = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database)
                .setConnectionPoolSize(16)
                .setConnectionMinimumIdleSize(4);
        if (StringUtils.hasText(password)) {
            single.setPassword(password);
        }
        int timeoutMs = parseTimeoutMillis(timeoutStr);
        single.setConnectTimeout(timeoutMs).setTimeout(timeoutMs);
        return Redisson.create(config);
    }

    /**
     * 将 "3000ms" / "3s" / "3000" 等配置字符串解析为毫秒。
     * 解析失败一律兜底 3000ms。
     */
    private int parseTimeoutMillis(String raw) {
        if (!StringUtils.hasText(raw)) {
            return 3000;
        }
        String text = raw.trim().toLowerCase();
        try {
            if (text.endsWith("ms")) {
                return Integer.parseInt(text.substring(0, text.length() - 2).trim());
            }
            if (text.endsWith("s")) {
                return Integer.parseInt(text.substring(0, text.length() - 1).trim()) * 1000;
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return 3000;
        }
    }
}
