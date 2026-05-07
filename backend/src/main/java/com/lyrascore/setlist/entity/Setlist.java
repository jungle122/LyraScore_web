package com.lyrascore.setlist.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Setlist {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String coverUrl;
    private LocalDateTime createdAt;
}
