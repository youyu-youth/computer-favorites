package com.yyyouth.service.mapper.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * (date, count) 单行映射，专给「按日期分组计数」类查询使用。
 *
 * 注意：MyBatis 列别名为 {@code d / c}，通过 @Results 显式映射。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateCountRow {

    /** 自然日 */
    private LocalDate d;

    /** 当日次数 */
    private Long c;
}
