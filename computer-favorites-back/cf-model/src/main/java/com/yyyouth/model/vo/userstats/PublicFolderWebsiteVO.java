package com.yyyouth.model.vo.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开主页对话框/查看全部页中的网站卡片（user-15 公开收藏夹）。
 *
 * <p>仅作展示用，不含点赞/收藏按钮所需的当前用户操作态；
 * tagNames 本期暂为 null（后续如需可由 Service 层从 t_website.tags 解析后填充）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFolderWebsiteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 网站ID */
    private Long id;

    /** 标题 */
    private String title;

    /** URL */
    private String url;

    /** 封面图 */
    private String cover;

    /** 简介 */
    private String description;

    /** 分类名 */
    private String categoryName;

    /** 标签名列表（本期为 null，预留扩展） */
    private List<String> tagNames;

    /** 点击数 */
    private Integer clickCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 评分（0-5，保留 1 位小数） */
    private BigDecimal score;
}
