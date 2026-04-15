package com.yyyouth.model.dto.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 我的举报列表查询参数
 */
@Data
public class ReportQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 类型筛选：1=网站，2=评论
     */
    private Integer type;

    /**
     * 状态筛选：0=待处理，1=已处理，2=已驳回
     */
    private Integer status;
}
