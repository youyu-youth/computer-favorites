package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-03
 *
 * 管理端标签分页结果
 */
@Data
public class AdminTagPageVO {

    /**
     * 列表数据
     */
    private List<AdminTagListItemVO> records;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Long totalPages;
}
