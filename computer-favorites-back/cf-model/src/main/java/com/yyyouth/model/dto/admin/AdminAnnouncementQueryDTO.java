package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-22
 *
 * 管理端公告查询参数
 */
@Data
public class AdminAnnouncementQueryDTO {

    /**
     * 关键字搜索（标题/内容）
     */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /**
     * 状态筛选：0隐藏，1显示
     */
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;

    /**
     * 类型筛选：1-新增内容，2-Bug修复，3-更新系统
     */
    @Min(value = 1, message = "类型值不合法")
    @Max(value = 3, message = "类型值不合法")
    private Integer type;

    /**
     * 置顶筛选：0否，1是
     */
    @Min(value = 0, message = "置顶值不合法")
    @Max(value = 1, message = "置顶值不合法")
    private Integer isTop;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 6;
}
