package com.yyyouth.common.constants;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 分类业务常量
 */
public class CategoryConstants {

    private CategoryConstants() {
    }

    /**
     * 根节点父级ID
     */
    public static final Long ROOT_PARENT_ID = 0L;

    /**
     * 分类禁用状态
     */
    public static final Integer STATUS_DISABLED = 0;

    /**
     * 分类启用状态
     */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * 逻辑未删除
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 逻辑已删除
     */
    public static final Integer DELETED = 1;

    /**
     * 默认排序值
     */
    public static final Integer DEFAULT_SORT = 0;
}
