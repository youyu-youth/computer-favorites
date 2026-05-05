package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论分页结果
 */
@Data
public class AdminCommentPageVO {

    /**
     * 列表数据
     */
    private List<AdminCommentListItemVO> records;

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
