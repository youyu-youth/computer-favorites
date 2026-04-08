package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户反馈查询参数
 */
@Data
public class UserFeedbackQueryDTO {

    /**
     * 页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量最大为50")
    private Integer pageSize = 20;

    /**
     * 状态：0待处理，1已处理，2已关闭
     */
    @Min(value = 0, message = "反馈状态不合法")
    @Max(value = 2, message = "反馈状态不合法")
    private Integer status;
}
