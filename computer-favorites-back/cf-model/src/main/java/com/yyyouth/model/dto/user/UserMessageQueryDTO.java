package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息查询参数
 */
@Data
public class UserMessageQueryDTO {

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
     * 已读状态：0未读，1已读
     */
    @Min(value = 0, message = "消息状态不合法")
    @Max(value = 1, message = "消息状态不合法")
    private Integer isRead;

    /**
     * 消息类型：1系统通知，2评论回复，3收藏提醒，4审核结果，5举报反馈
     */
    @Positive(message = "消息类型必须为正数")
    @Max(value = 5, message = "消息类型不合法")
    private Integer type;
}
