package com.yyyouth.model.vo.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 技术雷达 VO（6 维能力分布 + Top Languages 堆叠占比）
 * 对应 Redis key：user:profile:dashboard:{uid}:radar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechRadarVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 6 维能力雷达（前端 / 后端 / 数据库 / AI / 工程化 / 算法） */
    private List<TechRadarDimVO> dimensions;

    /** 用户技术栈语言占比（来自 t_user_profile.tech_stack） */
    private List<TechLanguageVO> languages;
}
