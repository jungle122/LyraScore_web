package com.lyrascore.log.service;

import com.lyrascore.common.BusinessException;
import com.lyrascore.log.dto.LogCreateRequest;
import com.lyrascore.log.entity.PracticeLog;
import com.lyrascore.log.event.PracticeLogCreatedEvent;
import com.lyrascore.log.mapper.PracticeLogMapper;
import com.lyrascore.score.entity.Score;
import com.lyrascore.score.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PracticeLogService {

    private final PracticeLogMapper logMapper;
    private final ScoreMapper scoreMapper;
    private final ApplicationEventPublisher publisher;

    public Long create(LogCreateRequest req, Long userId) {
        Score s = scoreMapper.selectById(req.getScoreId());
        if (s == null || !s.getUserId().equals(userId)) {
            throw new BusinessException(1301, "乐谱不存在或无权使用");
        }
        PracticeLog log = new PracticeLog();
        log.setUserId(userId);
        log.setScoreId(req.getScoreId());
        log.setDurationMins(req.getDurationMins());
        log.setCurrentBpm(req.getCurrentBpm());
        log.setThoughts(req.getThoughts());
        logMapper.insert(log);

        // 发事件 → 任务 12 徽章模块订阅
        publisher.publishEvent(new PracticeLogCreatedEvent(this, userId, log.getId(), req.getDurationMins()));
        return log.getId();
    }

    public List<PracticeLog> listMine(Long userId, Integer limit) {
        return logMapper.selectByUser(userId, limit);
    }

    /** 聚合统计：总分钟、本周分钟、按乐谱分组、近 7 日分组 */
    public Map<String, Object> stats(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalMins", logMapper.sumMinutes(userId));

        // 本周开始（周一 00:00）
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        result.put("weekMins", logMapper.sumMinutesSince(userId, weekStart.atStartOfDay().toString()));

        result.put("byScore", logMapper.sumByScore(userId));

        LocalDate sevenDaysAgo = today.minusDays(6);
        result.put("byDay", logMapper.sumByDay(userId, sevenDaysAgo.atStartOfDay().toString()));
        return result;
    }
}
