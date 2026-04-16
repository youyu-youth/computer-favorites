package com.yyyouth.service.audit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 审计切面配置
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AuditAspectConfig {
}
