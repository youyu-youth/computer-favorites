package com.yyyouth.service.aichat.agent.thinking_agent;

import com.yyyouth.model.constants.ai.AgentConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    @Override
    protected String step() {
        try {
            boolean isAction = think();
            if (!isAction) {
                return AgentConstants.AGENT_STEP_RESULT;
            }
            return action();
        } catch (Exception e) {
            log.error("Error executing step:", e.getMessage());
            return AgentConstants.AGENT_STEP_RESULT_ERROR + e.getMessage();
        }
    }

    public abstract boolean think();

    public abstract String action();
}
