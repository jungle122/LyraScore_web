package com.lyrascore.plan.entity;

import lombok.Data;

@Data
public class PlanItem {
    private Long id;
    private Long planId;
    private Long scoreId;
    private Integer sortOrder;

    // JOIN 出来的乐谱字段
    private String scoreTitle;
    private String scoreArtist;
    private String scoreImageUrl;
}
