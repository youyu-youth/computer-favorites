package com.yyyouth.web.controller.agent;

import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.agent.AgentPlanConfirmRequest;
import com.yyyouth.model.vo.agent.AgentPlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 执行计划确认/拒绝接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentPlanController {

    @PostMapping("/plan/confirm")
    public HttpResult  confirmPlan(@RequestBody @Valid AgentPlanConfirmRequest request) {
        log.info("确认执行计划: planId={}", request.getPlanId());
        AgentPlanVO vo = AgentPlanVO.builder()
                .planId(request.getPlanId())
                .status("executed")
                .build();

        return HttpResult.success(vo);
    }

    @PostMapping("/plan/reject")
    public HttpResult rejectPlan(@RequestBody @Valid AgentPlanConfirmRequest request) {
        log.info("拒绝执行计划: planId={}", request.getPlanId());
        AgentPlanVO vo = AgentPlanVO.builder()
                .planId(request.getPlanId())
                .status("rejected")
                .build();
        return HttpResult.success(vo);
    }
}
