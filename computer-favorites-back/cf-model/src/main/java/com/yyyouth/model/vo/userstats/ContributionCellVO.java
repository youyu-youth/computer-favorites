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
 * 贡献热力图单元格（嵌套于 ContributionGraphVO.cells）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionCellVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期（yyyy-MM-dd） */
    private String date;

    /** 当天贡献次数（行为总和） */
    private Integer count;

    /** 颜色等级：0 无 / 1 低 / 2 中低 / 3 中 / 4 高（共 5 阶） */
    private Integer level;
}
