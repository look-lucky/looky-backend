package com.looky.domain.store.dto;

import com.looky.domain.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HotStoreResponse {
    private Long storeId; // 상점 ID
    private String name; // 상점 이름
    private List<String> categories; // 상점 카테고리 목록
    private String benefitContent; // 혜택 내용 (제휴 혜택 > 쿠폰 혜택 > null)
    private Long favoriteGain; // 이번 주 찜 증가 수
    private String profileImageUrl; // 가게 프로필 이미지 URL

    public static HotStoreResponse from(Store store, Long favoriteGain, String benefitContent) {
        return HotStoreResponse.builder()
                .storeId(store.getId())
                .name(store.getName())
                .categories(store.getStoreCategories().stream().map(Enum::name).toList())
                .benefitContent(benefitContent)
                .favoriteGain(favoriteGain)
                .profileImageUrl(store.getProfileImageUrl())
                .build();
    }
}
