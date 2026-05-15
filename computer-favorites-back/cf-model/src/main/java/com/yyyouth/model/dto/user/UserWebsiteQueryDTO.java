package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站列表查询参数
 */
@Data
public class UserWebsiteQueryDTO {

    /**
     * 分类ID
     */
    @Positive(message = "分类ID必须为正数")
    private Long categoryId;

    /**
     * 关键字
     */
    @Size(max = 100, message = "关键字长度不能超过100")
    private String keyword;

    /**
     * 标签ID列表
     */
    @Size(max = 20, message = "标签筛选数量不能超过20")
    private List<@Positive(message = "标签ID必须为正数") Long> tagIds;

    /**
     * 排序字段：collectCount / clickCount / shelfTime
     */
    @Pattern(regexp = "^(collectCount|clickCount|shelfTime)$", message = "排序字段不合法")
    private String sortField;

    /**
     * 排序方式：-1 降序 / 1 升序
     */
    @Min(value = -1, message = "排序方式不合法")
    @Max(value = 1, message = "排序方式不合法")
    private Integer sortOrder;

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
