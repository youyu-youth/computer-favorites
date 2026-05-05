package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论查询参数
 */
@Data
public class AdminCommentQueryDTO {

    /**
     * 关键字（搜索用户名、评论内容）
     */
    @Size(max = 100, message = "关键字长度不能超过100")
    private String keyword;

    /**
     * 评论状态：0隐藏，1显示
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
    private Integer pageSize = 10;
}
