package com.lyrascore.setlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetlistCreateRequest {

    @NotBlank(message = "歌单名称不能为空")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @Size(max = 500)
    private String coverUrl;
}
