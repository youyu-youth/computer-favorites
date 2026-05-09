package com.yyyouth.core;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 项目后端启动入口类
 */
@ComponentScan("com.yyyouth")
@SpringBootApplication(scanBasePackages = {"com.yyyouth"})
@MapperScan("com.yyyouth.service.mapper")
@EnableScheduling
public class ComputerFavoritesApplication {
    /**
     * 应用启动方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ComputerFavoritesApplication.class, args);
    }
}
