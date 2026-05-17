package com.yyyouth.service.aichat.hook;

import com.yyyouth.model.pojo.agent.AgentHookEvent;
import com.yyyouth.service.mapper.AgentHookEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent 钩子事件发布器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HookEventPublisher {

    private final AgentHookEventMapper hookEventMapper;

    public void publish(String hookType, Long taskId, String handlerCode, String eventPayloadJson) {
        AgentHookEvent event = new AgentHookEvent();
        event.setTaskId(taskId);
        event.setHookType(hookType);
        event.setHandlerCode(handlerCode);
        event.setEventPayloadJson(eventPayloadJson);
        event.setStatus("succeeded");
        hookEventMapper.insert(event);
        log.debug("钩子事件发布: type={}, taskId={}", hookType, taskId);
    }

    public void publishError(String hookType, Long taskId, String errorMsg) {
        AgentHookEvent event = new AgentHookEvent();
        event.setTaskId(taskId);
        event.setHookType(hookType);
        event.setStatus("failed");
        event.setErrorMsg(errorMsg);
        hookEventMapper.insert(event);
    }
}
