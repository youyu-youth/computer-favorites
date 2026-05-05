package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 点赞列表分页结果
 */
@Data
@Builder
public class WebsiteLikePageVO {

    /**
     * 分页数据
     */
    private List<WebsiteLikeItemVO> records;

    /**
     * 总条数
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
