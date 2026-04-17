package com.yyyouth.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.vo.user.UserFeedbackImageUploadVO;
import com.yyyouth.service.user.notification.UserFeedbackService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.UserFeedbackController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈接口集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserFeedbackControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MockMvc mockMvc;

    @Mock
    private UserFeedbackService userFeedbackService;

    @InjectMocks
    private UserFeedbackController userFeedbackController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userFeedbackController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 上传反馈图片成功时应返回上传结果
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldUploadFeedbackImageSuccessfully() throws Exception {
        UserFeedbackImageUploadVO uploadVO = new UserFeedbackImageUploadVO();
        uploadVO.setObjectKey("user/feedback/image/202604/test.png");
        uploadVO.setImageUrl("https://minio.local/user/feedback/image/202604/test.png");
        when(userFeedbackService.uploadFeedbackImage(any())).thenReturn(uploadVO);

        MockMultipartFile file = new MockMultipartFile("file", "feedback.png", "image/png", "feedback".getBytes());

        mockMvc.perform(multipart("/api/user/feedbacks/images").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.objectKey").value("user/feedback/image/202604/test.png"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://minio.local/user/feedback/image/202604/test.png"));
    }

    /**
     * 提交反馈成功时应返回成功消息
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldSubmitFeedbackSuccessfully() throws Exception {
        String requestBody = """
                {
                  "type": 1,
                  "content": "这个功能很好，但是移动端弹窗关闭按钮不够明显。",
                  "contact": "tester@example.com",
                  "images": ["https://cdn.example.com/feedback/1.png"]
                }
                """;

        mockMvc.perform(post("/api/user/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("反馈提交成功"));

        verify(userFeedbackService).submitFeedback(any());
    }

    /**
     * 提交反馈内容过短时应返回参数错误
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBadRequestWhenFeedbackContentTooShort() throws Exception {
        String requestBody = """
                {
                  "type": 1,
                  "content": "太短了"
                }
                """;

        mockMvc.perform(post("/api/user/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath("$.msg").value("反馈内容长度需在10-500字符之间"));
    }

    /**
     * 提交反馈图片数量超限时应返回参数错误
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBadRequestWhenFeedbackImagesExceeded() throws Exception {
        String requestBody = """
                {
                  "type": 2,
                  "content": "这里反馈一个可复现的问题，已经整理了足够的描述信息。",
                  "images": [
                    "https://cdn.example.com/1.png",
                    "https://cdn.example.com/2.png",
                    "https://cdn.example.com/3.png",
                    "https://cdn.example.com/4.png",
                    "https://cdn.example.com/5.png",
                    "https://cdn.example.com/6.png"
                  ]
                }
                """;

        mockMvc.perform(post("/api/user/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath("$.msg").value("最多上传5张图片"));
    }

    /**
     * 删除反馈图片成功时应返回成功消息
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldDeleteFeedbackImageSuccessfully() throws Exception {
        doNothing().when(userFeedbackService).deleteFeedbackImage("user/feedback/image/202604/test.png");

        mockMvc.perform(delete("/api/user/feedbacks/images")
                        .param("objectKey", "user/feedback/image/202604/test.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("删除成功"));
    }
}
