package com.yyyouth.model.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * 通知事件消息体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String title;

    private String content;

    private Integer type;

    private Long relatedId;

    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
