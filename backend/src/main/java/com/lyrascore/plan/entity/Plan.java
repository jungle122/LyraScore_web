package com.lyrascore.plan.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Plan {
    private Long id;
    private Long userId;
    private String title;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;
}
