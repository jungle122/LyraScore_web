package com.lyrascore.setlist.dto;

import com.lyrascore.setlist.entity.Setlist;
import com.lyrascore.setlist.entity.SetlistItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SetlistDetailVO {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String coverUrl;
    private LocalDateTime createdAt;
    private List<SetlistItem> items;

    public static SetlistDetailVO from(Setlist s, List<SetlistItem> items) {
        SetlistDetailVO v = new SetlistDetailVO();
        v.setId(s.getId());
        v.setUserId(s.getUserId());
        v.setName(s.getName());
        v.setDescription(s.getDescription());
        v.setCoverUrl(s.getCoverUrl());
        v.setCreatedAt(s.getCreatedAt());
        v.setItems(items);
        return v;
    }
}
