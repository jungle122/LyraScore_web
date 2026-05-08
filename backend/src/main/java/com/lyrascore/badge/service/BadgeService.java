package com.lyrascore.badge.service;

import com.lyrascore.badge.entity.Badge;
import com.lyrascore.badge.mapper.BadgeMapper;
import com.lyrascore.log.mapper.PracticeLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeMapper badgeMapper;
    private final PracticeLogMapper logMapper;

    public List<Badge> listForUser(Long userId) {
        return badgeMapper.selectAllWithUserState(userId);
    }

    /**
     * 评估并授予所有满足条件的徽章。
     * 由 PracticeLogBadgeListener / 上传乐谱场景调用。
     */
    public void evaluateAndAward(Long userId) {
        List<Badge> all = badgeMapper.selectAll();
        Set<Long> owned = new HashSet<>(badgeMapper.selectBadgeIdsOfUser(userId));

        Long totalMins = logMapper.sumMinutes(userId);
        if (totalMins == null) totalMins = 0L;
        Long scoreCount = badgeMapper.countScoresOfUser(userId);
        if (scoreCount == null) scoreCount = 0L;
        boolean hasAnyLog = totalMins > 0;

        for (Badge b : all) {
            if (owned.contains(b.getId())) continue;
            boolean qualify = switch (b.getConditionType()) {
                case "first_log"     -> hasAnyLog;
                case "total_minutes" -> totalMins >= b.getConditionValue();
                case "score_count"   -> scoreCount >= b.getConditionValue();
                default -> false;
            };
            if (qualify) {
                int rows = badgeMapper.award(userId, b.getId());
                if (rows > 0) {
                    log.info("徽章授予 user={} badge={} ({})", userId, b.getName(), b.getConditionType());
                }
            }
        }
    }
}
