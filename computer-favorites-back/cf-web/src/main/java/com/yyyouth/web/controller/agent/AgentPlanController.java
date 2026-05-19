package com.yyyouth.web.controller.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.agent.AgentPlanConfirmRequest;
import com.yyyouth.model.vo.agent.AgentPlanVO;
import com.yyyouth.service.aichat.plan.AgentPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 执行计划确认/拒绝接口。
 * 用户确认后 AgentPlanService 会唤醒阻塞中的 ConfirmableToolCallback 继续执行。
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentPlanController {

    private final AgentPlanService agentPlanService;

    @PostMapping("/plan/confirm")
    public HttpResult confirmPlan(@RequestBody @Valid AgentPlanConfirmRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();

        agentPlanService.confirm(request.getPlanId(), "user", userId);

        AgentPlanVO vo = AgentPlanVO.builder()
                .planId(request.getPlanId())
                .status("approved")
                .build();
        return HttpResult.success(vo);
    }

    @PostMapping("/plan/reject")
    public HttpResult rejectPlan(@RequestBody @Valid AgentPlanConfirmRequest request) {
        StpUtil.checkLogin();
        String reason = "用户主动拒绝";
        agentPlanService.reject(request.getPlanId(), reason);

        AgentPlanVO vo = AgentPlanVO.builder()
                .planId(request.getPlanId())
                .status("rejected")
                .build();
        return HttpResult.success(vo);
    }
}
