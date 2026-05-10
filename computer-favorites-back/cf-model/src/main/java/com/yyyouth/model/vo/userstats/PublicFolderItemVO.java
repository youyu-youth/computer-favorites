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
 * 公开主页顶层收藏夹列表项（user-15 公开收藏夹）。
 *
 * <p>用于 ProfileSidebarCard 侧边栏 + PublicFolderDialog 顶层入口展示。
 * 仅含元信息和聚合计数，访客不可见的子项不计入 childrenCount。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFolderItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件夹ID */
    private Long id;

    /** 名称 */
    private String name;

    /** 图标（标识符或 emoji） */
    private String icon;

    /** 颜色（HEX，含 #） */
    private String color;

    /** 父文件夹ID（顶层=0） */
    private Long parentId;

    /** 排序值（升序） */
    private Integer sort;

    /** 文件夹下网站数量（来自 t_user_folder.website_count） */
    private Integer websiteCount;

    /** 子文件夹数量（仅可见的；他人视图已过滤 is_public=0 与 is_hide=1） */
    private Integer childrenCount;
}
