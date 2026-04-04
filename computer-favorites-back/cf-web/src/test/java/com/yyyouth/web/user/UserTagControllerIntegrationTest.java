package com.yyyouth.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.model.vo.user.UserTagListItemVO;
import com.yyyouth.model.vo.user.UserTagPageVO;
import com.yyyouth.service.user.tag.UserTagService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.UserTagController;
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
 * 用户端标签控制器集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserTagControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private UserTagService userTagService;

    @InjectMocks
    private UserTagController userTagController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userTagController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 查询标签分页列表成功应返回分页结构
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnTagPageSuccessfully() throws Exception {
        UserTagListItemVO itemVO = new UserTagListItemVO();
        itemVO.setId(1L);
        itemVO.setName("Vue");
        itemVO.setColor("#42B883");
        itemVO.setUseCount(12);

        UserTagPageVO pageVO = new UserTagPageVO();
        pageVO.setRecords(List.of(itemVO));
        pageVO.setTotal(1L);
        pageVO.setPageNum(1);
        pageVO.setPageSize(20);
        pageVO.setTotalPages(1L);

        when(userTagService.queryTagPage(any())).thenReturn(pageVO);

        mockMvc.perform(get("/api/tag/list")
                        .param("pageNum", "1")
                        .param("pageSize", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("Vue"))
                .andExpect(jsonPath("$.data.records[0].color").value("#42B883"));

        verify(userTagService).queryTagPage(any());
    }
}