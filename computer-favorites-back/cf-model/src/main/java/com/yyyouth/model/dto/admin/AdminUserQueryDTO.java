package com.yyyouth.model.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-09
 *
 * 管理端用户列表查询参数
 */
@Data
public class AdminUserQueryDTO {

    /**
     * 关键字（用户名/邮箱/昵称模糊搜索）
     */
    @Size(max = 100, message = "关键字长度不能超过100")
    private String keyword;

    /**
     * 状态筛选：0禁用，1正常，null=全部
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
