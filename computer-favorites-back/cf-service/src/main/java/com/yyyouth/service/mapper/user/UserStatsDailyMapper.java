package com.yyyouth.service.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.user.UserStatsDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户日粒度贡献明细 Mapper - user-15 用户主页
 */
@Mapper
public interface UserStatsDailyMapper extends BaseMapper<UserStatsDaily> {

    /**
     * 查询当日已累计贡献分（用于单日 50 分上限校验）
     */
    @Select("SELECT IFNULL(contribution, 0) FROM t_user_stats_daily " +
            "WHERE user_id = #{userId} AND stat_date = #{statDate} LIMIT 1")
    BigDecimal selectContribution(@Param("userId") Long userId,
                                  @Param("statDate") LocalDate statDate);

    /**
     * 增量 upsert：若 (user_id, stat_date) 不存在则插入，存在则各计数列累加 + 贡献分累加。
     * 调用方传入 6 个 column 各自 delta（默认 0），以及 contributionDelta（已经过单日上限裁剪）。
     */
    @Update("INSERT INTO t_user_stats_daily " +
            "(user_id, stat_date, submit_count, comment_count, collect_count, like_count, score_count, browse_count, contribution) " +
            "VALUES (#{userId}, #{statDate}, #{submitDelta}, #{commentDelta}, #{collectDelta}, #{likeDelta}, #{scoreDelta}, #{browseDelta}, #{contributionDelta}) " +
            "ON DUPLICATE KEY UPDATE " +
            "submit_count = submit_count + #{submitDelta}, " +
            "comment_count = comment_count + #{commentDelta}, " +
            "collect_count = collect_count + #{collectDelta}, " +
            "like_count = like_count + #{likeDelta}, " +
            "score_count = score_count + #{scoreDelta}, " +
            "browse_count = browse_count + #{browseDelta}, " +
            "contribution = contribution + #{contributionDelta}")
    int upsertIncr(@Param("userId") Long userId,
                   @Param("statDate") LocalDate statDate,
                   @Param("submitDelta") int submitDelta,
                   @Param("commentDelta") int commentDelta,
                   @Param("collectDelta") int collectDelta,
                   @Param("likeDelta") int likeDelta,
                   @Param("scoreDelta") int scoreDelta,
                   @Param("browseDelta") int browseDelta,
                   @Param("contributionDelta") BigDecimal contributionDelta);
}
