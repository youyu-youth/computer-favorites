package com.yyyouth.service.aichat.agent.thinking_agent;

import com.yyyouth.service.aichat.agent.advisor.LoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class MyManus extends ToolCallAgent {

    public MyManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setAgentName("MyManus");
        String SYSTEM_PROMPT = """
                You are MyManus, an all-capable AI assistant.
                You have various tools at your disposal to complete complex requests.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool.
                For complex tasks, break down the problem and use different tools step by step.
                After using each tool, clearly explain the execution results.
                If you want to stop, use the terminate tool.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new LoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
