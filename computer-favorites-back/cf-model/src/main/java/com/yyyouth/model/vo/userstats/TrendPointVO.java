package com.yyyouth.model.vo.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 趋势折线单点（嵌套于 TrendSeriesVO.points）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期（yyyy-MM-dd） */
    private String date;

    /** 当天指标值 */
    private BigDecimal value;
}
