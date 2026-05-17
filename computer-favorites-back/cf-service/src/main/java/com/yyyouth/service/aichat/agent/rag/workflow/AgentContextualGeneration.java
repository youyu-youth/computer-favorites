package com.yyyouth.service.aichat.agent.rag.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.stereotype.Component;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/23
 * @Description: 上下文增强器
 * 对该项目的功能还为开发完
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AgentContextualGeneration {


    /**
     *  空上下文处理
     * @return
     */
    public ContextualQueryAugmenter emptyContextHandle() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                有问题可以联系编程导航客服 https://codefather.cn
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();

    }

}
