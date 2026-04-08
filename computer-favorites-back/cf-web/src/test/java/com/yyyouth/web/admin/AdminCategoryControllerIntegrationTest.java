package com.yyyouth.web.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.model.dto.admin.AdminCategoryCreateDTO;
import com.yyyouth.model.vo.admin.AdminCategoryTreeItemVO;
import com.yyyouth.service.admin.category.AdminCategoryService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.admin.AdminCategoryController;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类控制器集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminCategoryControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @Mock
    private AdminCategoryService adminCategoryService;

    @InjectMocks
    private AdminCategoryController adminCategoryController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminCategoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 查询分类树应返回成功响应
     */
    @Test
    void shouldReturnTreeWhenQueryCategoryTree() throws Exception {
        AdminCategoryTreeItemVO root = new AdminCategoryTreeItemVO();
        root.setId(1L);
        root.setName("前端开发");

        when(adminCategoryService.queryCategoryTree(eq(1))).thenReturn(List.of(root));

        mockMvc.perform(get("/api/admin/category/tree").param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("前端开发"));

        verify(adminCategoryService).queryCategoryTree(eq(1));
    }

    /**
     * 创建分类成功应返回分类ID
     */
    @Test
    void shouldReturnCategoryIdWhenCreateCategory() throws Exception {
        when(adminCategoryService.createCategory(any(AdminCategoryCreateDTO.class))).thenReturn(100L);

        AdminCategoryCreateDTO createDTO = new AdminCategoryCreateDTO();
        createDTO.setName("Vue");
        createDTO.setParentId(0L);
        createDTO.setSort(0);

        mockMvc.perform(post("/api/admin/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("创建成功"))
                .andExpect(jsonPath("$.data").value(100));

        verify(adminCategoryService).createCategory(any(AdminCategoryCreateDTO.class));
    }

    /**
     * 创建分类名称为空应返回参数校验失败
     */
    @Test
    void shouldReturnBadRequestWhenCreateCategoryWithBlankName() throws Exception {
        AdminCategoryCreateDTO createDTO = new AdminCategoryCreateDTO();
        createDTO.setName(" ");
        createDTO.setParentId(0L);
        createDTO.setSort(0);

        mockMvc.perform(post("/api/admin/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath("$.msg").value("分类名称不能为空"));
    }

    /**
     * 删除分类应返回成功响应
     */
    @Test
    void shouldReturnSuccessWhenDeleteCategory() throws Exception {
        doNothing().when(adminCategoryService).deleteCategory(eq(10L));

        mockMvc.perform(delete("/api/admin/category/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("删除成功"));

        verify(adminCategoryService).deleteCategory(eq(10L));
    }
}
