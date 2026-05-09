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
 * 单个分类占比项（嵌套于 CategoryDistributionVO.items）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryShareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类 ID（Other 聚合项为 null） */
    private Long categoryId;

    /** 分类名称 */
    private String name;

    /** 累计权重得分 */
    private BigDecimal weight;

    /** 占比百分比（0-100，保留 1 位小数） */
    private BigDecimal pct;
}
