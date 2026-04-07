package com.yyyouth.model.vo.user;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿分页结果
 */
@Data
public class UserWebsiteSubmissionPageVO {

    /**
     * 列表数据
     */
    private List<UserWebsiteSubmissionListItemVO> records;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Long totalPages;
}
