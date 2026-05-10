package com.yyyouth.model.vo.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开主页对话框/查看全部页中的子收藏夹（user-15 公开收藏夹）。
 *
 * <p>不含 childrenCount —— 对话框规则为「一层平铺、不可下钻」，子收藏夹的孙级数量
 * 不需要在对话框内呈现，全量树由 PublicFolderTreeVO 单独承载。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFolderChildVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件夹ID */
    private Long id;

    /** 名称 */
    private String name;

    /** 图标 */
    private String icon;

    /** 颜色 */
    private String color;

    /** 父文件夹ID */
    private Long parentId;

    /** 文件夹下网站数量 */
    private Integer websiteCount;
}
