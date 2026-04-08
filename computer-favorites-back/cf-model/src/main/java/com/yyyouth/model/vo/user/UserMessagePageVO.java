package com.yyyouth.model.vo.user;

import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息分页结果
 */
@Data
public class UserMessagePageVO {

    /**
     * 消息列表
     */
    private List<UserMessageItemVO> list;

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
     * 未读总数
     */
    private Long unreadCount;
}
