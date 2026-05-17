package com.yyyouth.model.enums.ai;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent业务任务状态枚举
 */
public enum TaskStatus {
    PLANNING,
    WAIT_CONFIRM,
    EXECUTING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
