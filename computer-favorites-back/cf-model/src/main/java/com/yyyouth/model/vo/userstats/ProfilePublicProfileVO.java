package com.yyyouth.model.vo.userstats;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 公开主页 - 个人资料（脱敏，user-15 M5）。
 *
 * 仅保留对外展示字段：签名 / 地理位置 / 社交链接 / 兴趣标签 / 技术栈。
 * 不下发：favoriteWebsites / uploadedWebsites / contribution（私密统计字段）。
 */
@Data
@Builder
public class ProfilePublicProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String signature;

    private String country;

    private String city;

    private String githubUrl;

    private String giteeUrl;

    private String blogUrl;

    /** 兴趣标签（CSV / JSON 字符串，由前端解析） */
    private String hobbyTags;

    /** 技术栈 ID 串（CSV，由前端 / 看板接口 join 字典展示） */
    private String techStack;
}
