package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
