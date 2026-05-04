package com.yyyouth.web.controller.aichat;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/29
 */
@SpringBootTest
class TestAiControllerTest {


    @Resource
    ChatClient chatClient;

    @Test
    void chat() {
        ChatResponse chatResponse = chatClient.prompt().user("你好").call().chatResponse();
        System.out.println("结果："+chatResponse.getResult().getOutput().getText());
    }
}