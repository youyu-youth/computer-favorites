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
 * 分类偏好分布 VO（环形图 + 柱状图共用）
 * TOP8 + 其他聚合
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总权重（用于前端绝对值展示） */
    private java.math.BigDecimal totalWeight;

    /** 分类项（按 weight 降序，最后一项可能是 "其他"） */
    private List<CategoryShareVO> items;
}
