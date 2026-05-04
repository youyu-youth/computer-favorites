package com.yyyouth.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 我的评论分页视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCommentPageVO {

    /**
     * 评论列表
     */
    private List<MyCommentItemVO> records;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;
}
