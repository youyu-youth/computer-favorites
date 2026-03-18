package com.yyyouth.core.auth;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.core.ComputerFavoritesApplication;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.auth.UserSession;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.auth.UserSessionMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 认证真实链路集成测试
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(
        classes = ComputerFavoritesApplication.class,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
        }
)
class AuthFlowIntegrationTest {

    private static final Long USER_ID = 1001L;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAccountMapper userAccountMapper;

    @MockBean
    private UserSessionMapper userSessionMapper;

    @MockBean
    private UserProfileMapper userProfileMapper;

    @MockBean
    private UserSettingMapper userSettingMapper;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, UserAccount.class);
        TableInfoHelper.initTableInfo(builderAssistant, UserSession.class);
        TableInfoHelper.initTableInfo(builderAssistant, UserProfile.class);
    }

    /**
     * 每个用例准备基础账号数据
     */
    @BeforeEach
    void setUp() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(USER_ID);
        userAccount.setUsername("tester");
        userAccount.setNickname("测试用户");
        userAccount.setEmail("tester@test.com");
        userAccount.setStatus(1);
        userAccount.setDeleted(0);
        userAccount.setPasswordHash(new BCryptPasswordEncoder().encode("123456"));
        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(userSessionMapper.selectOne(any())).thenReturn(null);
    }

    /**
     * 登录后应可完成续期、会话查询与退出
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldCompleteAuthSessionFlowWhenLoginSuccess() throws Exception {
        Map<String, Object> loginReq = new HashMap<>();
        loginReq.put("username", "tester");
        loginReq.put("password", "123456");
        loginReq.put("deviceType", "web");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.tokenValue").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String tokenName = loginJson.path("data").path("tokenName").asText("satoken");
        String tokenValue = loginJson.path("data").path("tokenValue").asText();

        mockMvc.perform(put("/api/auth/session/renew").header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("续期成功"));

        mockMvc.perform(get("/api/auth/session/current").header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.tokenValue").value(tokenValue));

        mockMvc.perform(delete("/api/auth/session").header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("退出成功"));
    }

    /**
     * 注册接口应完成用户与资料初始化
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldRegisterAndInitUserDataWhenEmailNotExists() throws Exception {
        when(userAccountMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            UserAccount account = invocation.getArgument(0);
            account.setId(2002L);
            account.setCreateTime(LocalDateTime.now());
            return 1;
        }).when(userAccountMapper).insert(any(UserAccount.class));

        Map<String, Object> registerReq = new HashMap<>();
        registerReq.put("email", "new-user@test.com");
        registerReq.put("password", "123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("注册成功"));

        verify(userAccountMapper, times(1)).insert(any(UserAccount.class));
        verify(userProfileMapper, times(1)).insert(any(UserProfile.class));
        verify(userSettingMapper, times(1)).insert(any(UserSetting.class));
    }

    /**
     * 邮箱已注册时应返回业务错误码
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBusinessErrorWhenRegisterEmailExists() throws Exception {
        UserAccount existAccount = new UserAccount();
        existAccount.setId(3003L);
        when(userAccountMapper.selectOne(any())).thenReturn(existAccount);

        Map<String, Object> registerReq = new HashMap<>();
        registerReq.put("email", "exist@test.com");
        registerReq.put("password", "123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.REGISTER_EMAIL_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(AuthErrorCode.REGISTER_EMAIL_EXISTS.getMessage()));
    }
}
