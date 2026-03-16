package com.yyyouth.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * Sa-Token拦截配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/api/**")
                .notMatch("/api/auth/login")
                .check(r -> StpUtil.checkLogin()))).addPathPatterns("/**");
    }
}
