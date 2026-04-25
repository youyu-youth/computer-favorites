package com.yyyouth.model.vo.user;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-23
 *
 * 用户公告分页结果
 */
@Data
public class UserAnnouncementPageVO {

    /**
     * 公告列表
     */
    private List<UserAnnouncementListItemVO> list;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Long totalPages;
}
