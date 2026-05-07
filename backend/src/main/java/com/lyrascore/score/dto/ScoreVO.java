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
    private String imageUrl;
    private String tuning;
    private Integer capo;
    private Integer bpm;
    private Integer isPublic;
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
        v.setMemo(s.getMemo());
        v.setCreatedAt(s.getCreatedAt());
        return v;
    }
}
