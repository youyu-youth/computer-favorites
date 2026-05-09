package com.yyyouth.model.vo.userstats;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 公开主页 - 隐私开关元信息（user-15 M5）。
 *
 * 只读元信息，仅用于前端正确渲染空态（如 show_contribution=0 时隐藏热力图卡片）。
 * 后端在置空字段后仍会下发开关本身，让前端决定空态文案。
 */
@Data
@Builder
public class ProfilePublicPrivacyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** public / logged / private */
    private String profileVisibility;

    /** 0/1 — 是否对他人展示贡献相关板块 */
    private Integer showContribution;

    /** 0/1 — 是否对他人展示收藏列表 */
    private Integer showCollections;
}
