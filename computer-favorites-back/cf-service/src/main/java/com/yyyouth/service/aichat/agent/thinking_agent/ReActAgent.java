package com.yyyouth.service.aichat.agent.thinking_agent;

import com.yyyouth.superagent.constants.AgentConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/26
 * ReAct（Reasoning and Acting）模式的代理抽象类实现了 "思考-行动" 的循环模式
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent{


    /**
     * 单步执行
     * @return
     */
    @Override
    protected String step() {
        try {
            //1. 思考是否执行下一步
            boolean isAction = think();
            if (!isAction){
                return AgentConstants.AGENT_STEP_RESULT;
            }
            //2. 执行下一步
            return action();
        } catch (Exception e) {
            log.error("Error executing step:", e.getMessage());
            return AgentConstants.AGENT_STEP_RESULT_ERROR + e.getMessage();
        }
    }


    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();


    /**
     * 执行当前状态的行动
     *
     * @return 执行结果
     */
    public abstract String action();



}
