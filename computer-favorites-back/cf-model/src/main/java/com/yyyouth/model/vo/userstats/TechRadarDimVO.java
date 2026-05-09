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
 * 技术雷达单维度（嵌套于 TechRadarVO.dimensions）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechRadarDimVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 维度名：前端 / 后端 / 数据库 / AI / 工程化 / 算法 */
    private String name;

    /** 维度得分（0-100） */
    private Integer value;
}
