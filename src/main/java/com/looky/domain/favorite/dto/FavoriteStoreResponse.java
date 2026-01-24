package com.looky.domain.favorite.dto;

import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteStoreResponse {

    private Long storeId;
    private String name;
    private String address;
    private List<StoreCategory> storeCategories;
    private String imageUrl; // 대표이미지

    public static FavoriteStoreResponse from(Store store) {
        return FavoriteStoreResponse.builder()
                .storeId(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .storeCategories(new ArrayList<>(store.getStoreCategories()))
                .build();
    }
}
