package com.lyrascore.badge.listener;

import com.lyrascore.badge.service.BadgeService;
import com.lyrascore.log.event.PracticeLogCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 练习日志写入后自动评估徽章。
 * 用 Spring 的 @EventListener 实现领域事件解耦，
 * 替代 CLAUDE.md 一开始提的「DB 触发器」方案——
 * 业务逻辑留在 Java 里，可读、可测、可单步调试。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PracticeLogBadgeListener {

    private final BadgeService badgeService;

    @EventListener
    public void onPracticeLogCreated(PracticeLogCreatedEvent event) {
        log.debug("PracticeLogCreatedEvent received, userId={}", event.getUserId());
        badgeService.evaluateAndAward(event.getUserId());
    }
}
