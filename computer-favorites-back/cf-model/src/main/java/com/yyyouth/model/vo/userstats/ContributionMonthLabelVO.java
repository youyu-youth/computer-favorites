package com.yyyouth.model.vo.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 热力图月份标签（嵌套于 ContributionGraphVO.months）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionMonthLabelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 月份英文缩写：Jan / Feb / ... */
    private String label;

    /** 该月份起始周在 53 列网格中的列偏移（0-52） */
    private Integer colOffset;
}
