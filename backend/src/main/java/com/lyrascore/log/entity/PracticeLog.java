package com.lyrascore.log.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PracticeLog {
    private Long id;
    private Long userId;
    private Long scoreId;
    private Integer durationMins;
    private Integer currentBpm;
    private String thoughts;
    private LocalDateTime logTime;

    // JOIN 出来的乐谱信息
    private String scoreTitle;
    private String scoreArtist;
}
