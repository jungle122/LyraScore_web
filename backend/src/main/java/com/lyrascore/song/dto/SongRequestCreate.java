package com.lyrascore.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SongRequestCreate {

    @NotNull(message = "receiverId 不能为空")
    private Long receiverId;

    @NotBlank(message = "原文不能为空")
    @Size(max = 500)
    private String rawInput;

    @Size(max = 255)
    private String requestMessage;
}
