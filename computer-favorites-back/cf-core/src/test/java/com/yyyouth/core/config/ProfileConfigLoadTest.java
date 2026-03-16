package com.yyyouth.core.config;

import com.yyyouth.core.ComputerFavoritesApplication;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 多环境配置加载验证测试
 */
class ProfileConfigLoadTest {

    /**
     * 验证指定 profile 启动时加载到对应配置
     *
     * @param profile profile 名称
     * @param expectedPort 预期端口
     * @param expectedMarker 预期标识
     * @param expectedAuthEnable 预期鉴权开关
     */
    @ParameterizedTest
    @CsvSource({
        "local,18080,local,false",
        "dev,18081,dev,true",
        "test,19080,test,true"
    })
    void shouldLoadProfileConfigWhenApplicationStarts(String profile,
                                                      int expectedPort,
                                                      String expectedMarker,
                                                      boolean expectedAuthEnable) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(ComputerFavoritesApplication.class)
            .profiles(profile)
            .web(WebApplicationType.NONE)
            .run("--spring.main.banner-mode=off", "--spring.main.lazy-initialization=true")) {
            Environment environment = context.getEnvironment();
            assertThat(environment.getProperty("server.port", Integer.class)).isEqualTo(expectedPort);
            assertThat(environment.getProperty("cf.profile.marker")).isEqualTo(expectedMarker);
            assertThat(environment.getProperty("cf.auth.enable", Boolean.class)).isEqualTo(expectedAuthEnable);
        }
    }
}
