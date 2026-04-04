package com.yyyouth.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.user.UserWebsiteCategoryVO;
import com.yyyouth.model.vo.user.UserWebsiteDetailVO;
import com.yyyouth.model.vo.user.UserWebsiteListItemVO;
import com.yyyouth.model.vo.user.UserWebsitePageVO;
import com.yyyouth.service.user.website.UserWebsiteService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.UserWebsiteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站资源控制器集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserWebsiteControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private UserWebsiteService userWebsiteService;

    @InjectMocks
    private UserWebsiteController userWebsiteController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userWebsiteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 查询网站分页列表成功应返回分页结构
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnWebsitePageSuccessfully() throws Exception {
        UserWebsiteListItemVO itemVO = new UserWebsiteListItemVO();
        itemVO.setId(1L);
        itemVO.setName("Vue.js");
        itemVO.setCategoryName("Web & Frontend Development");
        itemVO.setClickCount(1200);
        itemVO.setLikeCount(560);
        itemVO.setTags(List.of("Vue3", "Tailwind"));

        UserWebsitePageVO pageVO = new UserWebsitePageVO();
        pageVO.setRecords(List.of(itemVO));
        pageVO.setTotal(1L);
        pageVO.setPageNum(1);
        pageVO.setPageSize(12);
        pageVO.setTotalPages(1L);

        when(userWebsiteService.queryWebsitePage(any())).thenReturn(pageVO);

        mockMvc.perform(get("/api/website/list")
                        .param("pageNum", "1")
                        .param("pageSize", "12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("Vue.js"))
                .andExpect(jsonPath("$.data.records[0].tags[0]").value("Vue3"));

        verify(userWebsiteService).queryWebsitePage(any());
    }

    /**
     * 查询分类统计成功应返回分类数据
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnCategoryStatsSuccessfully() throws Exception {
        UserWebsiteCategoryVO categoryVO = new UserWebsiteCategoryVO();
        categoryVO.setId(10L);
        categoryVO.setName("Web & Frontend Development");
        categoryVO.setCount(25L);

        when(userWebsiteService.queryCategoryStats()).thenReturn(List.of(categoryVO));

        mockMvc.perform(get("/api/website/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data[0].name").value("Web & Frontend Development"))
                .andExpect(jsonPath("$.data[0].count").value(25));

        verify(userWebsiteService).queryCategoryStats();
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
        detailVO.setTags(List.of("Vue", "UI"));

        when(userWebsiteService.queryWebsiteDetail(100L)).thenReturn(detailVO);

        mockMvc.perform(get("/api/website/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.name").value("PrimeVue"))
                .andExpect(jsonPath("$.data.tags[1]").value("UI"));

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
}
