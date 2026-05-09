package com.yyyouth.model.dto.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户行为事件 - user-15 用户主页贡献统计
 * 由 UserActivityPublisher 在 6 类业务行为同步落库后发往 exchange.user.stats，
 * 消费端 UserStatsConsumer 增量 upsert 三张快照表（daily/overview/tag-affinity）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 事件唯一 ID（雪花算法），用于消费幂等去重 */
    private String eventId;

    /** 用户 ID */
    private Long userId;

    /** 行为类型 code，对应 {@link com.yyyouth.model.enums.UserActivityType#getCode()} */
    private String type;

    /** 业务目标 ID（websiteId / commentId / submissionId 等，可空） */
    private Long targetId;

    /** 网站分类 ID，驱动 t_user_tag_affinity dim_type=1（可空） */
    private Long categoryId;

    /** 网站标签 ID 列表，驱动 t_user_tag_affinity dim_type=2（可空） */
    private List<Long> tagIds;

    /** 网站技术栈 ID 列表，驱动 t_user_tag_affinity dim_type=3（可空） */
    private List<Long> techIds;

    /** 事件触发时间 */
    @Builder.Default
    private LocalDateTime ts = LocalDateTime.now();
}
