package com.neardeal.domain.item.dto;

import com.neardeal.domain.item.entity.Item;
import com.neardeal.domain.store.entity.Store;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private int price;

    private String description;

    private boolean isSoldOut;

    public Item toEntity(Store store, String uploadedImageUrl) {
        return Item.builder()
                .store(store)
                .name(name)
                .price(price)
                .description(description)
                .imageUrl(uploadedImageUrl)
                .isSoldOut(isSoldOut)
                .build();
    }
}
