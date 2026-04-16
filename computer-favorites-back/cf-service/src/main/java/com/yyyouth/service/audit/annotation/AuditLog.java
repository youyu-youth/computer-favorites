package com.yyyouth.service.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 审计日志注解
 */
@Inherited
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 操作模块
     *
     * @return 模块名称
     */
    String module() default "";

    /**
     * 操作动作
     *
     * @return 动作名称
     */
    String action() default "";

    /**
     * 操作描述，支持 SpEL
     *
     * @return 描述信息
     */
    String description() default "";

    /**
     * 是否忽略返回值记录
     *
     * @return 是否忽略
     */
    boolean ignoreResult() default false;

    /**
     * 发生异常时是否忽略审计日志
     *
     * @return 是否忽略
     */
    boolean ignoreOnException() default false;
}
