package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站标签项
 */
@Data
public class UserWebsiteTagItemVO {

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
}