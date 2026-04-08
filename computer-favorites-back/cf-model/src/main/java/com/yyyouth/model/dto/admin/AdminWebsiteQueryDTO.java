package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-01
 *
 * 管理端网站列表查询参数
 */
@Data
public class AdminWebsiteQueryDTO {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 关键字
     */
    @Size(max = 100, message = "关键字长度不能超过100")
    private String keyword;

    /**
     * 审核状态：0待审核，1已通过，2已拒绝
     */
    @Min(value = 0, message = "审核状态参数不合法")
    @Max(value = 2, message = "审核状态参数不合法")
    private Integer auditStatus;

    /**
     * 审核分组：0待审核，1已审核（已通过+已拒绝）
     */
    @Min(value = 0, message = "审核分组参数不合法")
    @Max(value = 1, message = "审核分组参数不合法")
    private Integer auditBucket;

    /**
     * 删除筛选：-1全部，0未删除，1已删除
     */
    @Min(value = -1, message = "删除筛选参数不合法")
    @Max(value = 1, message = "删除筛选参数不合法")
    private Integer deleted = -1;

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
    private Integer pageSize = 12;
}
