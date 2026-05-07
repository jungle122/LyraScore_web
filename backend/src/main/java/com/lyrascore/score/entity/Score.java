package com.lyrascore.score.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Score {
    private Long id;
    private Long userId;
    private String title;
    private String artist;
    private String imageUrl;
    private String tuning;
    private Integer capo;
    private Integer bpm;
    private Integer isPublic;
    private String memo;
    private LocalDateTime createdAt;
}
