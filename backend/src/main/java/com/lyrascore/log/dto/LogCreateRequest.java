package com.lyrascore.log.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LogCreateRequest {

    @NotNull(message = "scoreId 不能为空")
    private Long scoreId;

    @NotNull(message = "练习时长不能为空")
    @Min(value = 1, message = "练习时长至少 1 分钟")
    @Max(value = 1440, message = "练习时长不超过 1440 分钟")
    private Integer durationMins;

    @Min(0) @Max(400)
    private Integer currentBpm;

    @Size(max = 500)
    private String thoughts;
}
