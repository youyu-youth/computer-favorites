package com.yyyouth.service.notification;

import com.yyyouth.model.dto.notification.NotifyEvent;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * 通知发送服务
 */
public interface MessageNotifyService {

    /**
     * 异步发送通知事件
     *
     * @param event 通知事件
     */
    void send(NotifyEvent event);

    /**
     * 批量异步发送通知事件
     *
     * @param events 通知事件列表
     */
    void sendBatch(List<NotifyEvent> events);
}
