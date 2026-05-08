package com.lyrascore.score.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScoreUpdateRequest {

    @NotBlank(message = "歌曲名不能为空")
    @Size(max = 100)
    private String title;

    @Size(max = 100)
    private String artist;

    @Size(max = 50)
    private String tuning;

    private Integer capo;
    private Integer bpm;
    private Integer isPublic;

    @Size(max = 20)
    private String instrument;

    @Size(max = 20)
    private String style;

    private String memo;
}
