package com.lyrascore.log.mapper;

import com.lyrascore.log.entity.PracticeLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PracticeLogMapper {

    int insert(PracticeLog log);

    List<PracticeLog> selectByUser(@Param("userId") Long userId, @Param("limit") Integer limit);

    Long sumMinutes(@Param("userId") Long userId);

    Long sumMinutesSince(@Param("userId") Long userId, @Param("since") String sinceDate);

    // 按乐谱分组：教学示范 GROUP BY + JOIN
    List<Map<String, Object>> sumByScore(@Param("userId") Long userId);

    // 近 7 天按日聚合
    List<Map<String, Object>> sumByDay(@Param("userId") Long userId, @Param("since") String sinceDate);
}
