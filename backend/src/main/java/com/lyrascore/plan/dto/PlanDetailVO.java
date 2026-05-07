package com.lyrascore.plan.dto;

import com.lyrascore.plan.entity.Plan;
import com.lyrascore.plan.entity.PlanItem;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PlanDetailVO {
    private Long id;
    private Long userId;
    private String title;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<PlanItem> items;

    public static PlanDetailVO from(Plan p, List<PlanItem> items) {
        PlanDetailVO v = new PlanDetailVO();
        v.setId(p.getId());
        v.setUserId(p.getUserId());
        v.setTitle(p.getTitle());
        v.setStatus(p.getStatus());
        v.setStartDate(p.getStartDate());
        v.setEndDate(p.getEndDate());
        v.setItems(items);
        return v;
    }
}
