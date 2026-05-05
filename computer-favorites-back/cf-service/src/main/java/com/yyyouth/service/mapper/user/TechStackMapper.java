package com.yyyouth.service.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.user.TechStack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-04-15
 *
 * 技术栈字典 Mapper
 */
@Mapper
public interface TechStackMapper extends BaseMapper<TechStack> {

    /**
     * 分页查询技术栈（含关联用户数子查询）
     *
     * @param keyword 关键字
     * @param status 状态筛选
     * @param sortSql 排序SQL片段（白名单映射，防止注入）
     * @param offset 偏移量
     * @param pageSize 每页大小
     * @return 技术栈列表（Map形式）
     */
    @Select("<script>" +
            "SELECT ts.id, ts.name, ts.icon_png, ts.official_url, ts.description, " +
            "ts.color, ts.status, ts.sort, ts.create_time, ts.update_time, " +
            "(SELECT COUNT(*) FROM t_user_profile up WHERE up.deleted = 0 AND FIND_IN_SET(ts.id, up.tech_stack)) AS user_count " +
            "FROM t_tech_stack ts " +
            "WHERE ts.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (ts.name LIKE CONCAT('%',#{keyword},'%') OR ts.description LIKE CONCAT('%',#{keyword},'%')) " +
            "</if> " +
            "<if test='status != null'>" +
            "  AND ts.status = #{status} " +
            "</if> " +
            "ORDER BY ${sortSql} " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Map<String, Object>> selectTechStackPageWithUserCount(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("sortSql") String sortSql,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /**
     * 分页计数查询
     *
     * @param keyword 关键字
     * @param status 状态筛选
     * @return 总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_tech_stack " +
            "WHERE deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (name LIKE CONCAT('%',#{keyword},'%') OR description LIKE CONCAT('%',#{keyword},'%')) " +
            "</if> " +
            "<if test='status != null'>" +
            "  AND status = #{status} " +
            "</if> " +
            "</script>")
    long countTechStackPage(@Param("keyword") String keyword, @Param("status") Integer status);

    /**
     * 统计技术栈关联用户数
     *
     * @param techStackId 技术栈ID
     * @return 关联用户数
     */
    @Select("SELECT COUNT(*) FROM t_user_profile WHERE deleted = 0 AND FIND_IN_SET(#{techStackId}, tech_stack)")
    long countUsersByTechStackId(@Param("techStackId") Long techStackId);
}
