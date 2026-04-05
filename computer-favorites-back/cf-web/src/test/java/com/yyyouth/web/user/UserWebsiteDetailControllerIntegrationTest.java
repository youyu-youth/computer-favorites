package com.yyyouth.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.user.UserWebsiteDetailVO;
import com.yyyouth.model.vo.user.UserWebsiteTagItemVO;
import com.yyyouth.service.user.website.UserWebsiteService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.UserWebsiteDetailController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户端网站详情控制器集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserWebsiteDetailControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private UserWebsiteService userWebsiteService;

    @InjectMocks
    private UserWebsiteDetailController userWebsiteDetailController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userWebsiteDetailController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 查询网站详情成功应返回详情数据
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnWebsiteDetailSuccessfully() throws Exception {
        UserWebsiteDetailVO detailVO = new UserWebsiteDetailVO();
        detailVO.setId(100L);
        detailVO.setName("PrimeVue");
        detailVO.setCategoryName("UI Library");
        detailVO.setIsOfficial(1);
        detailVO.setIsRecommend(1);
        detailVO.setSubmitterId(101L);
        detailVO.setProviderName("前端架构师");
        detailVO.setShelfTime(LocalDateTime.of(2026, 4, 5, 9, 30, 0));

        UserWebsiteTagItemVO primeVueTag = new UserWebsiteTagItemVO();
        primeVueTag.setId(201L);
        primeVueTag.setName("Vue");
        primeVueTag.setColor("#42B883");

        UserWebsiteTagItemVO uiTag = new UserWebsiteTagItemVO();
        uiTag.setId(202L);
        uiTag.setName("UI");
        uiTag.setColor("#334155");

        detailVO.setTags(List.of(primeVueTag, uiTag));

        when(userWebsiteService.queryWebsiteDetail(100L)).thenReturn(detailVO);

        mockMvc.perform(get("/api/website/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.name").value("PrimeVue"))
                .andExpect(jsonPath("$.data.isOfficial").value(1))
                .andExpect(jsonPath("$.data.isRecommend").value(1))
                .andExpect(jsonPath("$.data.submitterId").value(101))
                .andExpect(jsonPath("$.data.providerName").value("前端架构师"))
                .andExpect(jsonPath("$.data.shelfTime").exists())
                .andExpect(jsonPath("$.data.tags[1].name").value("UI"))
                .andExpect(jsonPath("$.data.tags[1].color").value("#334155"));

        verify(userWebsiteService).queryWebsiteDetail(100L);
    }

    /**
     * 查询不存在的网站详情应返回业务错误
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBusinessErrorWhenWebsiteNotFound() throws Exception {
        when(userWebsiteService.queryWebsiteDetail(999L))
                .thenThrow(new BusinessException(HttpStatus.BAD_REQUEST, "网站不存在或已下架"));

        mockMvc.perform(get("/api/website/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath("$.msg").value("网站不存在或已下架"));

        verify(userWebsiteService).queryWebsiteDetail(999L);
    }

    /**
     * 查询网站详情时ID不合法应返回参数校验错误
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnValidationErrorWhenWebsiteIdInvalid() throws Exception {
        mockMvc.perform(get("/api/website/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.ERROR));

        verify(userWebsiteService, never()).queryWebsiteDetail(org.mockito.ArgumentMatchers.anyLong());
    }
}
