package com.looky.domain.partnership.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StorePartnershipResponse {
    private String organizationName;
    private String benefit;
    private Boolean isMyBenefit;

    public static StorePartnershipResponse of(String organizationName, String benefit, Boolean isMyBenefit) {
        return StorePartnershipResponse.builder()
                .organizationName(organizationName)
                .benefit(benefit)
                .isMyBenefit(isMyBenefit)
                .build();
    }
}
