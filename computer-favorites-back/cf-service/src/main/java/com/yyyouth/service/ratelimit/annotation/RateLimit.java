package com.yyyouth.service.ratelimit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 接口限流注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流 key，支持 SpEL 模板表达式
     *
     * @return 限流 key
     */
    String key();

    /**
     * 窗口内允许次数
     *
     * @return 允许次数
     */
    int limit();

    /**
     * 窗口长度
     *
     * @return 窗口长度
     */
    long window();

    /**
     * 窗口时间单位
     *
     * @return 时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 限流提示
     *
     * @return 提示文案
     */
    String message() default "请求过于频繁，请稍后再试";
}
