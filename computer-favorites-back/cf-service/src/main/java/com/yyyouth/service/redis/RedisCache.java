package com.yyyouth.service.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * Redis Cache-Aside 工具类（user-15 看板首个落地点）。
 *
 * 提供能力：
 *  1. {@link #getOrLoad(String, Class, long, long, Supplier)} 读穿透 + 空值缓存防穿透 + 互斥单飞防击穿；
 *  2. {@link #evictByPattern(String)} 用 SCAN + UNLINK 异步清理 key（不阻塞 Redis 主线程）；
 *  3. TTL 自带随机抖动避免雪崩。
 *
 * 序列化：内部使用 Spring Boot 自动装配的 {@link ObjectMapper}，写入字符串值；
 * 任何 POJO 必须可被 Jackson 默认机制反序列化（含无参构造或 @Builder 兼容）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCache {

    /** 空值占位（防穿透） */
    private static final String NULL_PLACEHOLDER = "__NULL__";

    /** 空值缓存 TTL（秒） */
    private static final long NULL_CACHE_TTL_SECONDS = 60L;

    /** 互斥锁等待时间（毫秒，超过则降级直接回源） */
    private static final long LOCK_WAIT_MILLIS = 500L;

    /** 互斥锁持有时间（毫秒，回源 SQL 兜底超时） */
    private static final long LOCK_LEASE_MILLIS = 5_000L;

    /** SCAN 单批扫描数 */
    private static final int SCAN_BATCH = 100;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper;

    /**
     * Cache-Aside 读取：命中 → 反序列化返回；未命中 → 单飞回源 → 写回。
     *
     * @param key            Redis key（建议从 {@code RedisConstant} 拼接）
     * @param clazz          值类型
     * @param ttlSeconds     基础 TTL（秒）
     * @param jitterSeconds  随机抖动范围（±N 秒）
     * @param loader         回源 Supplier；返回 null 表示数据库也无数据，将写入空值占位
     * @param <T>            值类型
     * @return 缓存值或回源值；空值占位场景返回 null
     */
    public <T> T getOrLoad(String key, Class<T> clazz, long ttlSeconds, long jitterSeconds, Supplier<T> loader) {
        T cached = readSafely(key, clazz);
        if (cached != null) {
            return cached;
        }
        if (isNullPlaceholder(key)) {
            return null;
        }
        return loadWithMutex(key, clazz, ttlSeconds, jitterSeconds, loader);
    }

    /**
     * 同 {@link #getOrLoad(String, Class, long, long, Supplier)}，支持泛型 TypeReference（如 List/Map）。
     */
    public <T> T getOrLoad(String key, TypeReference<T> typeRef, long ttlSeconds, long jitterSeconds, Supplier<T> loader) {
        T cached = readSafely(key, typeRef);
        if (cached != null) {
            return cached;
        }
        if (isNullPlaceholder(key)) {
            return null;
        }
        return loadWithMutex(key, typeRef, ttlSeconds, jitterSeconds, loader);
    }

    /**
     * 主动失效单个 key
     */
    public void evict(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        try {
            stringRedisTemplate.unlink(key);
        } catch (Exception e) {
            log.warn("[redis-cache] evict 失败 key={}, err={}", key, e.getMessage());
        }
    }

    /**
     * 按 pattern 批量失效（用 SCAN + UNLINK 异步删除，不阻塞 Redis）
     */
    public void evictByPattern(String pattern) {
        if (!StringUtils.hasText(pattern)) {
            return;
        }
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(SCAN_BATCH).build();
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.warn("[redis-cache] scan 失败 pattern={}, err={}", pattern, e.getMessage());
            return;
        }
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        try {
            stringRedisTemplate.unlink(keys);
        } catch (Exception unlinkErr) {
            try {
                stringRedisTemplate.delete(keys);
            } catch (Exception delErr) {
                log.warn("[redis-cache] evictByPattern 删除失败 pattern={}, count={}, err={}",
                        pattern, keys.size(), delErr.getMessage());
            }
        }
    }

    /**
     * 直接写缓存（含抖动 TTL）
     */
    public <T> void set(String key, T value, long ttlSeconds, long jitterSeconds) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        try {
            String json = (value == null) ? NULL_PLACEHOLDER : objectMapper.writeValueAsString(value);
            long ttl = (value == null) ? NULL_CACHE_TTL_SECONDS : effectiveTtl(ttlSeconds, jitterSeconds);
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttl));
        } catch (Exception e) {
            log.warn("[redis-cache] set 失败 key={}, err={}", key, e.getMessage());
        }
    }

    // -------------------- 内部实现 --------------------

    private <T> T readSafely(String key, Class<T> clazz) {
        try {
            String raw = stringRedisTemplate.opsForValue().get(key);
            if (raw == null) {
                return null;
            }
            if (NULL_PLACEHOLDER.equals(raw)) {
                return null;
            }
            return objectMapper.readValue(raw, clazz);
        } catch (Exception e) {
            log.warn("[redis-cache] read 失败 key={}, err={}", key, e.getMessage());
            return null;
        }
    }

    private <T> T readSafely(String key, TypeReference<T> typeRef) {
        try {
            String raw = stringRedisTemplate.opsForValue().get(key);
            if (raw == null) {
                return null;
            }
            if (NULL_PLACEHOLDER.equals(raw)) {
                return null;
            }
            return objectMapper.readValue(raw, typeRef);
        } catch (Exception e) {
            log.warn("[redis-cache] read 失败 key={}, err={}", key, e.getMessage());
            return null;
        }
    }

    private boolean isNullPlaceholder(String key) {
        try {
            String raw = stringRedisTemplate.opsForValue().get(key);
            return NULL_PLACEHOLDER.equals(raw);
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T loadWithMutex(String key, Class<T> clazz, long ttlSeconds, long jitterSeconds, Supplier<T> loader) {
        RLock lock = redissonClient.getLock(buildLockKey(key));
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_MILLIS, LOCK_LEASE_MILLIS, TimeUnit.MILLISECONDS);
            if (locked) {
                T recheck = readSafely(key, clazz);
                if (recheck != null) {
                    return recheck;
                }
                return reloadAndCache(key, ttlSeconds, jitterSeconds, loader);
            }
            // 抢锁失败：再读一次（可能其他线程已写入），仍未命中则降级直查
            T recheck = readSafely(key, clazz);
            return recheck != null ? recheck : loader.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return loader.get();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private <T> T loadWithMutex(String key, TypeReference<T> typeRef, long ttlSeconds, long jitterSeconds, Supplier<T> loader) {
        RLock lock = redissonClient.getLock(buildLockKey(key));
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_MILLIS, LOCK_LEASE_MILLIS, TimeUnit.MILLISECONDS);
            if (locked) {
                T recheck = readSafely(key, typeRef);
                if (recheck != null) {
                    return recheck;
                }
                return reloadAndCache(key, ttlSeconds, jitterSeconds, loader);
            }
            T recheck = readSafely(key, typeRef);
            return recheck != null ? recheck : loader.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return loader.get();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private <T> T reloadAndCache(String key, long ttlSeconds, long jitterSeconds, Supplier<T> loader) {
        T loaded = loader.get();
        try {
            if (loaded == null) {
                stringRedisTemplate.opsForValue().set(key, NULL_PLACEHOLDER,
                        Duration.ofSeconds(NULL_CACHE_TTL_SECONDS));
            } else {
                String json = objectMapper.writeValueAsString(loaded);
                stringRedisTemplate.opsForValue().set(key, json,
                        Duration.ofSeconds(effectiveTtl(ttlSeconds, jitterSeconds)));
            }
        } catch (Exception e) {
            log.warn("[redis-cache] writeBack 失败 key={}, err={}", key, e.getMessage());
        }
        return loaded;
    }

    private String buildLockKey(String key) {
        return "lock:cache:" + key;
    }

    private long effectiveTtl(long ttlSeconds, long jitterSeconds) {
        if (ttlSeconds <= 0) {
            return 60L;
        }
        if (jitterSeconds <= 0) {
            return ttlSeconds;
        }
        long jitter = ThreadLocalRandom.current().nextLong(-jitterSeconds, jitterSeconds + 1);
        return Math.max(1L, ttlSeconds + jitter);
    }
}
