package com.lyrascore.score.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScoreCreateRequest {

    @NotBlank(message = "歌曲名不能为空")
    @Size(max = 100)
    private String title;

    @Size(max = 100)
    private String artist;

    @Size(max = 50)
    private String tuning;

    private Integer capo;

    private Integer bpm;

    private Integer isPublic = 0;

    private String memo;
}
