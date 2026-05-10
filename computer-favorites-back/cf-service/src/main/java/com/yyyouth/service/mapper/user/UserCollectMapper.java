package com.yyyouth.service.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyyouth.model.pojo.user.UserCollect;
import com.yyyouth.model.vo.userstats.PublicFolderWebsiteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏 Mapper
 */
@Mapper
public interface UserCollectMapper extends BaseMapper<UserCollect> {

    /**
     * 公开主页：按 user_id + folder_id 分页查询直属网站（user-15 公开收藏夹）。
     *
     * <p>必须显式带 {@code uc.user_id = #{userId}} 防越权读取。
     * 仅返回上架中（{@code w.status = 1}）且未删除（{@code w.deleted = 0}）的网站，
     * 与 UserCollectServiceImpl.collect 风格保持一致。
     *
     * @param page     分页参数
     * @param userId   收藏所属用户
     * @param folderId 收藏夹 ID
     * @return 公开网站卡片分页
     */
    @Select("""
            SELECT
                w.id              AS id,
                w.name            AS title,
                w.url             AS url,
                w.icon            AS cover,
                w.summary         AS description,
                w.click_count     AS clickCount,
                w.like_count      AS likeCount,
                w.collect_count   AS collectCount,
                w.score           AS score,
                c.name            AS categoryName
            FROM t_user_collect uc
            INNER JOIN t_website w ON w.id = uc.website_id
            LEFT JOIN t_category c ON c.id = w.category_id AND c.deleted = 0
            WHERE uc.user_id = #{userId}
              AND uc.folder_id = #{folderId}
              AND w.deleted = 0
              AND w.status = 1
            ORDER BY uc.create_time DESC
            """)
    IPage<PublicFolderWebsiteVO> selectPublicWebsitesByFolder(
            Page<PublicFolderWebsiteVO> page,
            @Param("userId") Long userId,
            @Param("folderId") Long folderId);
}
