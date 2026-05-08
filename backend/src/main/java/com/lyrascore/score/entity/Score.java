package com.lyrascore.score.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Score {
    private Long id;
    private Long userId;
    private String title;
    private String artist;
    private String imageUrl;        // 多张图用英文逗号分隔
    private String tuning;
    private Integer capo;
    private Integer bpm;
    private Integer isPublic;
    private Integer practiceStatus; // 0未开始 / 1正在练 / 2已练完
    private String instrument;      // 吉他 / 尤克里里 / 其他
    private String style;           // 弹唱 / 指弹 / 其他
    private String memo;
    private LocalDateTime createdAt;
}
