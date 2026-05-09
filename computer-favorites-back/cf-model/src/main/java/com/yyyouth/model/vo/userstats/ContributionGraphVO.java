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
 * 贡献热力图 VO（GitHub 风格 365 天网格）
 * 对应 Redis key：user:profile:dashboard:{uid}:graph:{year}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionGraphVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 年份 */
    private Integer year;

    /** 全年总贡献次数 */
    private Integer total;

    /** 单日最高贡献次数（用于颜色刻度归一） */
    private Integer maxDaily;

    /** 365/366 天的格子（按日期升序） */
    private List<ContributionCellVO> cells;

    /** 月份标签（用于网格上方的月份位置） */
    private List<ContributionMonthLabelVO> months;
}
