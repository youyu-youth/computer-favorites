package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站分类统计
 */
@Data
public class UserWebsiteCategoryVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 网站数量
     */
    private Long count;
}
