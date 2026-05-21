package com.yyyouth.model.dto.agent;

import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * Agent 工具返回协议。status 为 incomplete 时前端展示补全卡片，
 * submitted 时表示操作已完成。
 */
@Data
@Builder
public class ToolResult {

    /** incomplete | submitted */
    private String status;

    /** 已收集的字段 key → value */
    private Map<String, Object> collected;

    /** 缺失字段列表 */
    private List<MissingField> missing;

    /** 面向用户的提示文本 */
    private String message;

    @Data
    @Builder
    public static class MissingField {
        /** 字段名（对应 DTO 属性） */
        private String field;
        /** 中文标签 */
        private String label;
        /** text | select */
        private String type;
        /** 是否为必填 */
        private boolean required;
        /** 推荐值列表 */
        private List<Suggestion> suggestions;
    }

    @Data
    @Builder
    public static class Suggestion {
        private Object value;
        private String label;
    }

    /** 工厂：参数不完整 */
    public static ToolResult incomplete(Map<String, Object> collected,
                                         List<MissingField> missing,
                                         String message) {
        return ToolResult.builder()
                .status("incomplete")
                .collected(collected)
                .missing(missing)
                .message(message)
                .build();
    }

    /** 工厂：提交成功 */
    public static ToolResult submitted(Long websiteId, String name, String url) {
        return ToolResult.builder()
                .status("submitted")
                .message(String.format("网站投稿已提交成功！\n网站ID：%d\n名称：%s\nURL：%s\n请等待管理员审核。",
                        websiteId, name, url))
                .build();
    }

    public String toJson() {
        return JSONUtil.toJsonStr(this);
    }
}
