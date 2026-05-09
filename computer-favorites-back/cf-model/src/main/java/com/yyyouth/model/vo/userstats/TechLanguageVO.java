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
 * 技术雷达 - 语言/技术栈占比项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechLanguageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 技术 ID（来自 t_tech_stack；用户自定义则为 null） */
    private Long techId;

    /** 技术名 */
    private String name;

    /** 占比百分比（0-100，保留 1 位小数） */
    private BigDecimal pct;
}
