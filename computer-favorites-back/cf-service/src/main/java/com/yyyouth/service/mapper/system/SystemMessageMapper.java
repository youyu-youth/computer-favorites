package com.yyyouth.service.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.system.SystemMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 站内消息 Mapper
 */
@Mapper
public interface SystemMessageMapper extends BaseMapper<SystemMessage> {

    /**
     * 查询用户消息分页列表
     *
     * @param userId 接收用户ID
     * @param isRead 已读状态
     * @param type 消息类型
     * @param offset 偏移量
     * @param pageSize 每页条数
     * @return 消息列表
     */
    @Select({
	    "<script>",
	    "SELECT id, user_id, title, content, type, related_id, is_read, create_time",
	    "FROM t_message",
	    "WHERE user_id = #{userId}",
	    "<if test='isRead != null'>AND is_read = #{isRead}</if>",
	    "<if test='type != null'>AND type = #{type}</if>",
	    "ORDER BY is_read ASC, create_time DESC, id DESC",
	    "LIMIT #{offset}, #{pageSize}",
	    "</script>"
    })
    List<SystemMessage> selectUserMessagePage(
	    @Param("userId") Long userId,
	    @Param("isRead") Integer isRead,
	    @Param("type") Integer type,
	    @Param("offset") Integer offset,
	    @Param("pageSize") Integer pageSize
    );

    /**
     * 统计用户消息总数
     *
     * @param userId 接收用户ID
     * @param isRead 已读状态
     * @param type 消息类型
     * @return 总数
     */
    @Select({
	    "<script>",
	    "SELECT COUNT(1)",
	    "FROM t_message",
	    "WHERE user_id = #{userId}",
	    "<if test='isRead != null'>AND is_read = #{isRead}</if>",
	    "<if test='type != null'>AND type = #{type}</if>",
	    "</script>"
    })
    Long countUserMessages(
	    @Param("userId") Long userId,
	    @Param("isRead") Integer isRead,
	    @Param("type") Integer type
    );

    /**
     * 统计用户未读消息数量
     *
     * @param userId 接收用户ID
     * @return 未读数量
     */
    @Select("SELECT COUNT(1) FROM t_message WHERE user_id = #{userId} AND is_read = 0")
    Long countUnreadMessages(@Param("userId") Long userId);
}
