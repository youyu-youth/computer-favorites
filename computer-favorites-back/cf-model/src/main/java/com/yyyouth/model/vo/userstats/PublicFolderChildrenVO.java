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
 * 公开主页对话框：单收藏夹一层子项聚合（user-15 公开收藏夹）。
 *
 * <p>子文件夹一次返回不分页（顶层夹下子层数量预期较小）；
 * 直属网站走 MyBatis-Plus 分页（pageSize 默认 12，最大 50）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFolderChildrenVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 子收藏夹列表（不分页） */
    private List<PublicFolderChildVO> subFolders;

    /** 直属网站分页结果 */
    private WebsitePage websites;

    /**
     * 公开网站分页结果。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebsitePage implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 当前页网站列表 */
        private List<PublicFolderWebsiteVO> list;

        /** 总数 */
        private Long total;

        /** 当前页号（1 起） */
        private Integer pageNum;

        /** 每页大小 */
        private Integer pageSize;
    }
}
