package com.yyyouth.service.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.user.UserStatsOverview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户维度贡献快照 Mapper - user-15 用户主页
 */
@Mapper
public interface UserStatsOverviewMapper extends BaseMapper<UserStatsOverview> {

    /**
     * 增量 upsert：累计列累加 + last_active_date 更新为较新者。
     * level / streak / rank_percent 由 T+1 任务覆写，本方法不涉及。
     */
    @Update("INSERT INTO t_user_stats_overview " +
            "(user_id, total_submit, total_comment, total_collect, total_like, total_score, total_browse, total_contribution, last_active_date) " +
            "VALUES (#{userId}, #{submitDelta}, #{commentDelta}, #{collectDelta}, #{likeDelta}, #{scoreDelta}, #{browseDelta}, #{contributionDelta}, #{activeDate}) " +
            "ON DUPLICATE KEY UPDATE " +
            "total_submit = total_submit + #{submitDelta}, " +
            "total_comment = total_comment + #{commentDelta}, " +
            "total_collect = total_collect + #{collectDelta}, " +
            "total_like = total_like + #{likeDelta}, " +
            "total_score = total_score + #{scoreDelta}, " +
            "total_browse = total_browse + #{browseDelta}, " +
            "total_contribution = total_contribution + #{contributionDelta}, " +
            "last_active_date = GREATEST(IFNULL(last_active_date, #{activeDate}), #{activeDate})")
    int upsertIncr(@Param("userId") Long userId,
                   @Param("submitDelta") int submitDelta,
                   @Param("commentDelta") int commentDelta,
                   @Param("collectDelta") int collectDelta,
                   @Param("likeDelta") int likeDelta,
                   @Param("scoreDelta") int scoreDelta,
                   @Param("browseDelta") int browseDelta,
                   @Param("contributionDelta") BigDecimal contributionDelta,
                   @Param("activeDate") LocalDate activeDate);
}
