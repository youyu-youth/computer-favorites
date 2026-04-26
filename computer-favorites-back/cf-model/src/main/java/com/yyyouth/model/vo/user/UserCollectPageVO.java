package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏列表分页结果
 */
@Data
@Builder
public class UserCollectPageVO {

    private List<UserCollectItemVO> records;

    private Long total;

    private Integer pageNum;

    private Integer pageSize;

    private Integer totalPages;
}
