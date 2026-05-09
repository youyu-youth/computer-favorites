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
 * @date 2026-05-08
 *
 * 趋势折线 VO（最近 N 天单指标走势）
 * 对应 Redis key：user:profile:dashboard:{uid}:trend:{range}:{metric}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendSeriesVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 时间范围：7d / 30d / 90d */
    private String range;

    /** 指标名：pv / likes / favorites / comments / contribution */
    private String metric;

    /** 时间序列数据点（按日期升序） */
    private List<TrendPointVO> points;

    /** 与上一周期的环比变化百分比（保留 1 位小数；正负代表升降） */
    private BigDecimal delta;
}
