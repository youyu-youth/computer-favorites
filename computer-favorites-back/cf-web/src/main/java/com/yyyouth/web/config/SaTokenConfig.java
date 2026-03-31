package com.yyyouth.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * Sa-Token拦截配置
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthSecurityProperties.class)
public class SaTokenConfig implements WebMvcConfigurer {

    private final AuthSecurityProperties authSecurityProperties;

    /**
     * 注册Sa-Token拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!authSecurityProperties.isEnable()) {
            return;
        }
        registry.addInterceptor(new SaInterceptor(handle -> {
            String[] whitelistArray = CollectionUtils.isEmpty(authSecurityProperties.getWhitelist())
                    ? new String[0]
                    : authSecurityProperties.getWhitelist().toArray(new String[0]);
            SaRouter.match("/api/admin/**")
                .notMatch(whitelistArray)
                .check(r -> StpAdminUtil.checkLogin());
            SaRouter.match("/api/**")
                .notMatch("/api/admin/**")
                    .notMatch(whitelistArray)
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
