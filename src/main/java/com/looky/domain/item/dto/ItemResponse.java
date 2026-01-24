package com.looky.domain.item.dto;

import com.looky.domain.item.entity.Item;
import com.looky.domain.item.entity.ItemBadge;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemResponse {
    private Long id;
    private Long storeId;
    private String name;
    private int price;
    private String description;
    private String imageUrl;
    private boolean isSoldOut;
    private Integer itemOrder;
    private boolean isRepresentative;
    private boolean isHidden;
    private ItemBadge badge;

    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .storeId(item.getStore().getId())
                .name(item.getName())
                .price(item.getPrice())
                .description(item.getDescription())
                .imageUrl(item.getImageUrl())
                .isSoldOut(item.isSoldOut())
                .itemOrder(item.getItemOrder())
                .isRepresentative(item.isRepresentative())
                .isHidden(item.isHidden())
                .badge(item.getBadge())
                .build();
    }
}
