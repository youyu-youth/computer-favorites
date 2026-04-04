package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端标签分页查询参数
 */
@Data
public class UserTagQueryDTO {

    /**
     * 关键字
     */
    @Size(max = 50, message = "关键字长度不能超过50")
    private String keyword;

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
    private Integer pageSize = 30;

    /**
     * 排序字段
     */
    @Pattern(regexp = "^(id|name|color|useCount|createTime|updateTime)$", message = "排序字段不合法")
    private String sortField = "useCount";

    /**
     * 排序方向：1升序，-1降序
     */
    @Min(value = -1, message = "排序方向不合法")
    @Max(value = 1, message = "排序方向不合法")
    private Integer sortOrder = -1;

    /**
     * 校验排序方向只能为 1 或 -1
     *
     * @return 校验结果
     */
    @AssertTrue(message = "排序方向不合法")
    public boolean isSortOrderValid() {
        return sortOrder == null || sortOrder == 1 || sortOrder == -1;
    }
}