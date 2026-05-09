package com.yyyouth.service.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.user.UserTagAffinity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户标签 / 分类 / 技术栈偏好快照 Mapper - user-15 用户主页
 */
@Mapper
public interface UserTagAffinityMapper extends BaseMapper<UserTagAffinity> {

    /**
     * 增量 upsert：weight 累加 + hits +1。
     * dimName 在首次插入时落库；后续行为命中同一维度不更新名称（避免字典变更覆盖）。
     */
    @Update("INSERT INTO t_user_tag_affinity " +
            "(user_id, dim_type, dim_id, dim_name, weight, hits) " +
            "VALUES (#{userId}, #{dimType}, #{dimId}, #{dimName}, #{weightDelta}, 1) " +
            "ON DUPLICATE KEY UPDATE " +
            "weight = weight + #{weightDelta}, " +
            "hits = hits + 1")
    int upsertWeight(@Param("userId") Long userId,
                     @Param("dimType") int dimType,
                     @Param("dimId") Long dimId,
                     @Param("dimName") String dimName,
                     @Param("weightDelta") BigDecimal weightDelta);
}
