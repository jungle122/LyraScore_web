package com.lyrascore.badge.entity;

import lombok.Data;

@Data
public class Badge {
    private Long id;
    private String name;
    private String iconUrl;
    private String conditionType;   // first_log / total_minutes / score_count
    private Integer conditionValue;
    private String description;

    // 用户视角附加字段
    private Boolean unlocked;       // 当前用户是否已解锁
    private java.time.LocalDateTime unlockedAt;
}
