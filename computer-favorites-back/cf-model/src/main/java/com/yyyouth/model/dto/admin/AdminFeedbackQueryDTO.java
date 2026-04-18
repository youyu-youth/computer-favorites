package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈查询参数
 */
@Data
public class AdminFeedbackQueryDTO {

    /**
     * 关键字
     */
    @Size(max = 100, message = "关键字长度不能超过100")
    private String keyword;

    /**
     * 反馈状态
     */
    @Min(value = 0, message = "反馈状态参数不合法")
    @Max(value = 2, message = "反馈状态参数不合法")
    private Integer status;

    /**
     * 反馈类型
     */
    @Min(value = 1, message = "反馈类型参数不合法")
    @Max(value = 4, message = "反馈类型参数不合法")
    private Integer type;

    /**
     * 是否仅看带图反馈
     */
    private Boolean hasImages = Boolean.FALSE;

    /**
     * 是否仅看可回访反馈
     */
    private Boolean hasContact = Boolean.FALSE;

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
