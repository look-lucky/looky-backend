package com.looky.domain.storeclaim.dto;

import com.looky.domain.storeclaim.entity.StoreClaim;
import com.looky.domain.storeclaim.entity.StoreClaimStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
                .storeId(storeClaim.getStore().getId())
                .storeName(storeClaim.getStoreName())
                .representativeName(storeClaim.getRepresentativeName())
                .status(storeClaim.getStatus())
                .rejectReason(storeClaim.getStatus() == StoreClaimStatus.REJECTED ? storeClaim.getRejectReason() : null)
                .createdAt(storeClaim.getCreatedAt())
                .build();
    }
}
