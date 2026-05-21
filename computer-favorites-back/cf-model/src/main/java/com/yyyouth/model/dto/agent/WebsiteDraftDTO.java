package com.yyyouth.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * Agent 网站投稿工具参数 DTO，所有字段可选，LLM 按实际对话内容自由填充。
 * icon 和 summary 由后端自动推断，LLM 无需提供。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteDraftDTO {

    /** 网站名称 */
    private String name;

    /** 网站URL（必须以 http:// 或 https:// 开头） */
    private String url;

    /** 详细描述 */
    private String description;

    /** 分类ID */
    private Long categoryId;

    /** 标签名称，逗号分隔 */
    private String tags;
}
