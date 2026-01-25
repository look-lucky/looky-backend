package com.looky.domain.store.dto;

import com.looky.domain.store.entity.StoreClaim;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreClaimRequest {

    @NotNull(message = "가게 식별자는 필수입니다.")
    private Long storeId;
    
    @NotNull(message = "사용자 식별자는 필수입니다.")
    private Long userId;

    @NotNull(message = "사업자등록번호는 필수입니다.")
    private String bizRegNo;

    @NotNull(message = "대표자명은 필수입니다.")
    private String representativeName;

    @NotNull(message = "상호명은 필수입니다.")
    private String storeName;

    private String storePhone;

    public StoreClaim toEntity(String uploadedImageUrl) {
        return StoreClaim.builder()
                .storeId(storeId)
                .userId(userId)
                .bizRegNo(bizRegNo)
                .representativeName(representativeName)
                .storeName(storeName)
                .storePhone(storePhone)
                .licenseImageUrl(uploadedImageUrl)
                .build();
    }
}
