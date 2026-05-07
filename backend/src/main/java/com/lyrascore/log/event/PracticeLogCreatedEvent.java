package com.lyrascore.log.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 练习日志写入后发布的领域事件。
 * 任务 12 徽章模块用 @EventListener 订阅此事件实现自动发牌。
 */
@Getter
public class PracticeLogCreatedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long logId;
    private final Integer durationMins;

    public PracticeLogCreatedEvent(Object source, Long userId, Long logId, Integer durationMins) {
        super(source);
        this.userId = userId;
        this.logId = logId;
        this.durationMins = durationMins;
    }
}
