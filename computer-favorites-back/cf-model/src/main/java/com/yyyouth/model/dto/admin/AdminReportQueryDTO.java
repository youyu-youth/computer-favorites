package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报查询参数
 */
@Data
public class AdminReportQueryDTO {

    /**
     * 关键字
     */
    @Size(max = 100, message = "关键字长度不能超过100")
    private String keyword;

    /**
     * 仅看待处理
     */
    private Boolean pendingOnly = Boolean.FALSE;

    /**
     * 举报状态
     */
    @Min(value = 0, message = "举报状态参数不合法")
    @Max(value = 2, message = "举报状态参数不合法")
    private Integer status;

    /**
     * 举报类型
     */
    @Min(value = 1, message = "举报类型参数不合法")
    @Max(value = 2, message = "举报类型参数不合法")
    private Integer type;

    /**
     * 开始时间
     */
    @Size(max = 19, message = "开始时间格式不合法")
    private String startTime;

    /**
     * 结束时间
     */
    @Size(max = 19, message = "结束时间格式不合法")
    private String endTime;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 6;
}
