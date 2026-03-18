package com.yyyouth.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 认证安全配置属性
 */
@Data
@ConfigurationProperties(prefix = "cf.auth")
public class AuthSecurityProperties {

    /**
     * 是否启用统一登录拦截
     */
    private boolean enable = true;

    /**
     * 登录白名单
     */
    private List<String> whitelist = new ArrayList<>();
}
