package com.neardeal.domain.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateItemRequest {
    private String name;
    private Integer price;
    private String description;
    private Boolean isSoldOut;
}