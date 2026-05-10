package com.yyyouth.service.mapper.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * t_website 实时计数器行（仅供 {@link com.yyyouth.service.mapper.user.UploadImpactMapper} 内部使用）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteCounterRow {

    private Long id;

    /** 点击量（即浏览） */
    private Integer clickCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer commentCount;

    private Integer scoreCount;
}
