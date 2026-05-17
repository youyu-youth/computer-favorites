package com.yyyouth.service.aichat.user.impl;

import com.yyyouth.service.aichat.user.UserAiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/13
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserAiChatServiceImpl implements UserAiChatService {

    @Override
    public String chatTest(String userPrompt) {
        return "";
    }
}
