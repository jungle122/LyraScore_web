package com.lyrascore.dictionary.entity;

import lombok.Data;

@Data
public class Dictionary {
    private Long id;
    private String dictType;
    private String dictKey;
    private String dictValue;
    private Integer sortOrder;
    private Integer isActive;
}
