package com.yyyouth.model.vo.user;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端标签分页结果
 */
@Data
public class UserTagPageVO {

    /**
     * 列表数据
     */
    private List<UserTagListItemVO> records;

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