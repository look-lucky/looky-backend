package com.looky.domain.partnership.dto;

import com.looky.domain.organization.entity.OrganizationCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentPartnershipResponse {
    private OrganizationCategory organizationCategory;
    private String organizationName;
    private String benefit;
    private Boolean isMyBenefit;

    public static StudentPartnershipResponse of(OrganizationCategory organizationCategory, String organizationName, String benefit, Boolean isMyBenefit) {
        return StudentPartnershipResponse.builder()
                .organizationCategory(organizationCategory)
                .organizationName(organizationName)
                .benefit(benefit)
                .isMyBenefit(isMyBenefit)
                .build();
    }
}
