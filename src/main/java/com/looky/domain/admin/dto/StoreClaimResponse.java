package com.looky.domain.admin.dto;

import com.looky.domain.store.entity.StoreClaim;
import com.looky.domain.store.entity.StoreClaimStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoreClaimResponse {
    private Long id;
    private Long storeId;
    private Long userId;
    private String name;
    private String storeName;
    private String bizRegNo;
    private String representativeName;
    private String storePhone;
    private String licenseImageUrl;
    private StoreClaimStatus status;
    private LocalDateTime createdAt;
    private String adminMemo;

    public static StoreClaimResponse from(StoreClaim storeClaim, String name) {
        return StoreClaimResponse.builder()
                .id(storeClaim.getId())
                .storeId(storeClaim.getStoreId())
                .userId(storeClaim.getUserId())
                .name(name)
                .storeName(storeClaim.getStoreName())
                .bizRegNo(storeClaim.getBizRegNo())
                .representativeName(storeClaim.getRepresentativeName())
                .storePhone(storeClaim.getStorePhone())
                .licenseImageUrl(storeClaim.getLicenseImageUrl())
                .status(storeClaim.getStatus())
                .createdAt(storeClaim.getCreatedAt())
                .adminMemo(storeClaim.getAdminMemo())
                .build();
    }
}
