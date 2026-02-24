package com.looky.domain.partnership.dto;

import com.looky.domain.organization.entity.OrganizationCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StorePartnershipResponse {
    private OrganizationCategory organizationCategory;
    private String organizationName;
    private String benefit;
    private Boolean isMyBenefit;

    public static StorePartnershipResponse of(OrganizationCategory organizationCategory, String organizationName, String benefit, Boolean isMyBenefit) {
        return StorePartnershipResponse.builder()
                .organizationCategory(organizationCategory)
                .organizationName(organizationName)
                .benefit(benefit)
                .isMyBenefit(isMyBenefit)
                .build();
    }
}
