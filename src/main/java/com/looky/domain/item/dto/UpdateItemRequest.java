package com.looky.domain.item.dto;

import com.looky.domain.item.entity.ItemBadge;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateItemRequest {
    private String name;
    private Integer price;
    private String description;
    private Boolean isSoldOut;
    private Integer itemOrder;
    private Boolean isRepresentative;
    private Boolean isHidden;
    private ItemBadge badge;
}