package com.yyyouth.model.vo.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开主页全量公开收藏夹树（user-15 查看全部页用）。
 *
 * <p>树形结构，仅暴露元信息和 websiteCount —— 网站列表由对话框/侧栏二次拉取
 * （PublicFolderService.getFolderChildren），避免一次性返回过大体积。
 *
 * <p>他人视图：父节点对访客不可见时，整支挂掉（不暴露孤儿节点）；
 * 本人视图：保留所有节点，孤儿节点挂到 roots。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFolderTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 根节点列表（按 sort 升序） */
    private List<TreeNode> roots;

    /**
     * 公开收藏夹树节点。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreeNode implements Serializable {

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

        /** 排序值 */
        private Integer sort;

        /** 网站数量 */
        private Integer websiteCount;

        /** 子节点 */
        private List<TreeNode> children;
    }
}
