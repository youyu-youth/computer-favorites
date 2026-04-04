package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端标签列表项
 */
@Data
public class UserTagListItemVO {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 使用次数
     */
    private Integer useCount;
}