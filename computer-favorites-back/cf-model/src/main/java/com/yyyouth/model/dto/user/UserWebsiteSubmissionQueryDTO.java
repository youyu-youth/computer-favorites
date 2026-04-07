package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿列表查询参数
 */
@Data
public class UserWebsiteSubmissionQueryDTO {

    /**
     * 关键字
     */
    @Size(max = 100, message = "关键字长度不能超过100")
    private String keyword;

    /**
     * 审核状态：0待审核，1通过，2拒绝
     */
    @Min(value = 0, message = "审核状态参数不合法")
    @Max(value = 2, message = "审核状态参数不合法")
    private Integer auditStatus;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 12;
}
