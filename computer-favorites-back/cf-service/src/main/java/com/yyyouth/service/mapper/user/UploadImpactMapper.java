package com.yyyouth.service.mapper.user;

import com.yyyouth.service.mapper.user.dto.DateCountRow;
import com.yyyouth.service.mapper.user.dto.WebsiteCounterRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 上传网站影响力 - 只读 Mapper（user-15 扩展）。
 *
 * 提供 6 个方法：
 *  1. {@link #selectSitesBySubmitter(Long)}：拉取用户所有审核通过、未删除的上传网站含计数器列；
 *  2. {@link #countBrowse(List, LocalDateTime, LocalDateTime)}：浏览事件按日聚合；
 *  3. {@link #countLike(List, LocalDateTime, LocalDateTime)}：点赞事件按日聚合；
 *  4. {@link #countCollect(List, LocalDateTime, LocalDateTime)}：收藏事件按日聚合；
 *  5. {@link #countComment(List, LocalDateTime, LocalDateTime)}：评论事件按日聚合（含 deleted/status 过滤）；
 *  6. {@link #countScore(List, LocalDateTime, LocalDateTime)}：评分事件按日聚合。
 *
 * <p>SQL 用 {@code <script>} + {@code <foreach>} 动态拼 IN 子句；
 * 调用方必须保证 {@code websiteIds} 非空，否则 SQL 语法错误。
 *
 * <p>性能说明：5 张事件表本期均无 {@code (website_id, create_time)} 复合索引，
 * 大数据量场景需 P2 补索引；当前依赖 service 层 RedisCache TTL 顶住。
 */
@Mapper
public interface UploadImpactMapper {

    /**
     * 拿到用户所有审核通过、未删除的上传网站（含 5 计数器列）。
     */
    @Select("SELECT id, click_count, like_count, collect_count, comment_count, score_count "
            + "FROM t_website "
            + "WHERE submitter_id = #{userId} AND deleted = 0 AND audit_status = 1")
    List<WebsiteCounterRow> selectSitesBySubmitter(@Param("userId") Long userId);

    /**
     * 浏览：按日期分组计数（t_browse_history）
     */
    @Select("<script>"
            + "SELECT DATE(create_time) AS d, COUNT(*) AS c FROM t_browse_history "
            + "WHERE website_id IN "
            + "<foreach collection='websiteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "  AND create_time &gt;= #{from} AND create_time &lt; #{to} "
            + "GROUP BY DATE(create_time)"
            + "</script>")
    List<DateCountRow> countBrowse(@Param("websiteIds") List<Long> websiteIds,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    /**
     * 点赞：按日期分组计数（t_website_like）
     */
    @Select("<script>"
            + "SELECT DATE(create_time) AS d, COUNT(*) AS c FROM t_website_like "
            + "WHERE website_id IN "
            + "<foreach collection='websiteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "  AND create_time &gt;= #{from} AND create_time &lt; #{to} "
            + "GROUP BY DATE(create_time)"
            + "</script>")
    List<DateCountRow> countLike(@Param("websiteIds") List<Long> websiteIds,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    /**
     * 收藏：按日期分组计数（t_user_collect）
     */
    @Select("<script>"
            + "SELECT DATE(create_time) AS d, COUNT(*) AS c FROM t_user_collect "
            + "WHERE website_id IN "
            + "<foreach collection='websiteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "  AND create_time &gt;= #{from} AND create_time &lt; #{to} "
            + "GROUP BY DATE(create_time)"
            + "</script>")
    List<DateCountRow> countCollect(@Param("websiteIds") List<Long> websiteIds,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    /**
     * 评论：按日期分组计数（t_comment，过滤 deleted=0 AND status=1）
     */
    @Select("<script>"
            + "SELECT DATE(create_time) AS d, COUNT(*) AS c FROM t_comment "
            + "WHERE website_id IN "
            + "<foreach collection='websiteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "  AND create_time &gt;= #{from} AND create_time &lt; #{to} "
            + "  AND deleted = 0 AND status = 1 "
            + "GROUP BY DATE(create_time)"
            + "</script>")
    List<DateCountRow> countComment(@Param("websiteIds") List<Long> websiteIds,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    /**
     * 评分：按日期分组计数（t_website_score）
     */
    @Select("<script>"
            + "SELECT DATE(create_time) AS d, COUNT(*) AS c FROM t_website_score "
            + "WHERE website_id IN "
            + "<foreach collection='websiteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "  AND create_time &gt;= #{from} AND create_time &lt; #{to} "
            + "GROUP BY DATE(create_time)"
            + "</script>")
    List<DateCountRow> countScore(@Param("websiteIds") List<Long> websiteIds,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);
}
