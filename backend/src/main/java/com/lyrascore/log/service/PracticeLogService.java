package com.lyrascore.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyrascore.ai.AiService;
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
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public void delete(Long id, Long userId) {
        int rows = logMapper.deleteByIdAndUser(id, userId);
        if (rows == 0) {
            throw new com.lyrascore.common.BusinessException(1302, "日志不存在或无权删除");
        }
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

    /** 调 AI 生成练习总结；失败返回降级文本 */
    public Map<String, Object> generateReport(Long userId) {
        Map<String, Object> stats = stats(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("stats", stats);

        // 总练习分钟为 0 时直接降级，省一次 AI 调用
        Long total = (Long) stats.get("totalMins");
        if (total == null || total == 0) {
            result.put("report", "你还没有任何练习记录哦~ 上传第一首乐谱、记录第一次练习，Lyra Coach 下次就能帮你生成专属总结啦！");
            result.put("aiPowered", false);
            return result;
        }

        try {
            String statsJson = objectMapper.writeValueAsString(stats);
            String text = aiService.generatePracticeReport(statsJson);
            if (text != null && !text.isBlank()) {
                result.put("report", text);
                result.put("aiPowered", true);
            } else {
                // AI 失败降级：本地拼一段简洁总结
                result.put("report", buildFallbackReport(stats));
                result.put("aiPowered", false);
            }
        } catch (Exception e) {
            result.put("report", buildFallbackReport(stats));
            result.put("aiPowered", false);
        }
        return result;
    }

    private String buildFallbackReport(Map<String, Object> stats) {
        Long total = (Long) stats.getOrDefault("totalMins", 0L);
        Long week = (Long) stats.getOrDefault("weekMins", 0L);
        return String.format(
                "你累计已经练习了 %d 分钟，本周练习 %d 分钟。继续保持节奏，每天一点进步就够了，加油！",
                total, week);
    }
}
