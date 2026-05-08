package com.lyrascore.score.dto;

import com.lyrascore.score.entity.Score;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScoreVO {
    private Long id;
    private Long userId;
    private String title;
    private String artist;
    private String imageUrl;          // 多图用 , 分隔
    private String tuning;
    private Integer capo;
    private Integer bpm;
    private Integer isPublic;
    private Integer practiceStatus;
    private String instrument;
    private String style;
    private String memo;
    private LocalDateTime createdAt;

    public static ScoreVO from(Score s) {
        ScoreVO v = new ScoreVO();
        v.setId(s.getId());
        v.setUserId(s.getUserId());
        v.setTitle(s.getTitle());
        v.setArtist(s.getArtist());
        v.setImageUrl(s.getImageUrl());
        v.setTuning(s.getTuning());
        v.setCapo(s.getCapo());
        v.setBpm(s.getBpm());
        v.setIsPublic(s.getIsPublic());
        v.setPracticeStatus(s.getPracticeStatus());
        v.setInstrument(s.getInstrument());
        v.setStyle(s.getStyle());
        v.setMemo(s.getMemo());
        v.setCreatedAt(s.getCreatedAt());
        return v;
    }
}
