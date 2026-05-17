package com.yyyouth.service.aichat.agent.thinking_agent;

import cn.hutool.core.util.StrUtil;
import com.yyyouth.model.enums.ai.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/26 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Slf4j
@Data
public abstract class BaseAgent {

    /**
     * 代理名称
     */
    private String agentName;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 当前步骤
     */
    private int currentStep = 0;


    /**
     * 最大步骤数
     */
    private int maxSteps = 10;


    /**
     * 下一步提示词
     */
    private String nextStepPrompt;

    /**
     * 代理状态:默认为 空闲状态
     */
    private AgentState state = AgentState.IDLE;

    /**
     * LLM 模型
     */
    private ChatClient chatClient;

    /**
     * 上下文消息存储
     */
    private List<Message> contextMsgList;

    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        // 如果用户输入为空，则抛出异常
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        this.contextMsgList = new ArrayList<>();
        // 更改状态
        state = AgentState.RUNNING;
        //记录消息上下文
        contextMsgList.add(new UserMessage(userPrompt));
        //保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                // 单步执行
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                // 执行步骤
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误:" + e.getMessage();
        } finally {
            // 清理资源
            cleanUp();
        }

    }


    /**
     * 具体的单个步骤 ,由子类实现
     *
     * @return
     */
    protected abstract String step();


    /**
     * 清理资源
     */
    protected void cleanUp(){

    }

}
