package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端技术栈列表查询参数
 */
@Data
public class AdminTechStackQueryDTO {

    /**
     * 关键字
     */
    @Size(max = 50, message = "关键字长度不能超过50")
    private String keyword;

    /**
     * 状态筛选：0禁用，1正常，null全部
     */
    @Min(value = 0, message = "状态参数不合法")
    @Max(value = 1, message = "状态参数不合法")
    private Integer status;

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
    private Integer pageSize = 8;

    /**
     * 排序字段
     */
    @Pattern(regexp = "^(sort|name|status|createdAt|updatedAt)$", message = "排序字段不合法")
    private String sortField = "sort";

    /**
     * 排序方向：1升序，-1降序
     */
    @Min(value = -1, message = "排序方向不合法")
    @Max(value = 1, message = "排序方向不合法")
    private Integer sortOrder = 1;
}
