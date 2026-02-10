package com.looky.domain.item.dto;

import com.looky.domain.item.entity.ItemCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemCategoryResponse {
    private Long id;
    private Long storeId;
    private String name;

    public static ItemCategoryResponse from(ItemCategory category) {
        return ItemCategoryResponse.builder()
                .id(category.getId())
                .storeId(category.getStore().getId())
                .name(category.getName())
                .build();
    }
}
