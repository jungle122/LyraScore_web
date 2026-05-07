package com.lyrascore.setlist.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SetlistItem {
    private Long id;
    private Long setlistId;
    private Long scoreId;
    private LocalDateTime addedAt;

    // JOIN 出来的乐谱字段
    private String scoreTitle;
    private String scoreArtist;
    private String scoreImageUrl;
}
