package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 点赞列表分页查询参数
 */
@Data
public class WebsiteLikePageDTO {

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不合法")
    private Integer pageNum;

    /**
     * 每页条数
     */
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数不合法")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize;
}
