package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户公告查询参数
 */
@Data
public class UserAnnouncementQueryDTO {

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
     * 公告类型：1-新增内容，2-Bug修复，3-系统更新
     */
    @Min(value = 1, message = "类型值不合法")
    @Max(value = 3, message = "类型值不合法")
    private Integer type;

    /**
     * 是否置顶：0-否，1-是
     */
    @Min(value = 0, message = "置顶值不合法")
    @Max(value = 1, message = "置顶值不合法")
    private Integer isTop;
}
