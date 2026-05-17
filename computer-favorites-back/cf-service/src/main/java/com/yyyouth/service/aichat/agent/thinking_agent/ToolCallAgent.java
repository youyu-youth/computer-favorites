package com.yyyouth.service.aichat.agent.thinking_agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.yyyouth.model.enums.ai.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/26 处理工具调用的基础代理类，具体实现了think和act 方法，可以用作创建实例的父类
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class ToolCallAgent extends ReActAgent {

    // 可用工具列表
    private ToolCallback[] availableTools;

    //保存工具调用信息的相应结果
    private ChatResponse toolCallResponse;

    //工具调用管理者  （管理工具调用的）
    private ToolCallingManager toolCallingManager;

    //警用内置的工具调用，配置对象
    private ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] toolCallbacks) {
        this.availableTools = toolCallbacks;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)  // 禁用内置的工具调用
                .build();
    }


    /**
     * 思考
     * @return
     */
    @Override
    public boolean think() {
        //1. 判断是否需要执行下一步
        if (StrUtil.isNotEmpty(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            //1.1 将用户输入添加到上下文消息列表中
            getContextMsgList().add(userMessage);
        }

        //获取上下文信息
        List<Message> contextMessages = getContextMsgList();
        Prompt prompt = Prompt.builder()
                .messages(contextMessages)
                .chatOptions(this.chatOptions)
                .build();

        try {
            //2. 调用LLM模型,并获取响应
            ChatClient chatClient = getChatClient();
            ChatResponse chatResponse = chatClient.prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(this.availableTools)
                    .call()
                    .chatResponse();

            //3. 记录响应结果，用于是否执行action方法
            toolCallResponse = chatResponse;

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

            //获取输出提示信息
            String result = assistantMessage.getText();
            //获取工具调用信息,这里并不是说agent已经使用工具，而是是否需要调用工具
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();

            log.info(getAgentName() + "的思考：" + result);
            log.info(getAgentName() + "选择了" + toolCalls.size() + "个工具来使用");


            String toolCallInfo = toolCalls.stream()
                    .map(toolCall -> String.format("工具名称：%s,工具参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));

            log.info(getAgentName() + "的工具调用信息：" + toolCallInfo);


            //只有不调用工具的时候，才记录助手消息
            if (toolCalls.isEmpty()) {
                //记录助手消息
                getContextMsgList().add(assistantMessage);
                return false;
            } else {
                //需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getAgentName() + "的思考过程遇到了问题：" + e.getMessage());
            getContextMsgList().add(
                    new AssistantMessage("处理时遇到错误：" + e.getMessage()));
            return false;
        }

    }

    /**
     * 执行工具调用
     * @return
     */
    @Override
    public String action() {
        if (!toolCallResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getContextMsgList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallResponse);
        // 由于是手动调用工具，我们需要记录上下文，conversationHistory 已经包含了助手消息和工具返回的调用结果
        setContextMsgList(toolExecutionResult.conversationHistory());
        // 获取当前工具的调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String result = toolResponseMessage.getResponses()
                .stream()
                .map(response -> "工具:" + response.name() + "完成了它的任务！ 结果是：" + response.responseData())
                .collect(Collectors.joining("\n"));
        //判断是否调用了终止工具
        boolean isTerminate = toolResponseMessage.getResponses()
                .stream()
                .anyMatch(toolResponse -> "doTerminate".equals(toolResponse.name()));
        if (isTerminate){
            setState(AgentState.FINISHED);
        }
        log.info(result);

        return result;
    }
}
