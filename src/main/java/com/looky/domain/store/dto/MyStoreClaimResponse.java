package com.looky.domain.store.dto;

import com.looky.domain.store.entity.StoreClaim;
import com.looky.domain.store.entity.StoreClaimStatus;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyStoreClaimResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private String representativeName;
    private StoreClaimStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static MyStoreClaimResponse from(StoreClaim storeClaim) {
        return MyStoreClaimResponse.builder()
                .id(storeClaim.getId())
                .storeId(storeClaim.getStoreId())
                .storeName(storeClaim.getStoreName())
                .representativeName(storeClaim.getRepresentativeName())
                .status(storeClaim.getStatus())
                .rejectReason(storeClaim.getStatus() == StoreClaimStatus.REJECTED ? storeClaim.getRejectReason() : null)
                .createdAt(storeClaim.getCreatedAt())
                .build();
    }
}
