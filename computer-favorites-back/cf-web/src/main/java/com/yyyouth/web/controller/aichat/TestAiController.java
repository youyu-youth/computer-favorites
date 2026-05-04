package com.yyyouth.web.controller.aichat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/29
 */
@RequestMapping("/ai/")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TestAiController {

    private final ChatClient chatClient;


    @GetMapping("chat/{message}")
    public String chat(@PathVariable String message){
        log.info("用户请求Ai聊天，message={}", message);
        ChatResponse chatResponse = chatClient.prompt().user(message).call().chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }
}
