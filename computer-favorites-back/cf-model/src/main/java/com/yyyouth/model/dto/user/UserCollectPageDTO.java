package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏列表分页查询参数
 */
@Data
public class UserCollectPageDTO {

    private Long folderId;

    private String keyword;

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不合法")
    private Integer pageNum;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数不合法")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize;
}
