package com.lyrascore.admin.dto;

import lombok.Data;

@Data
public class DashboardStatsVO {
    private long userCount;
    private long scoreCount;
    private long planCount;
    private long logCount;
    private long setlistCount;
    private long friendshipCount;
    private long songRequestCount;
    private long badgeAwardedCount;
    private long totalPracticeMinutes;
}
