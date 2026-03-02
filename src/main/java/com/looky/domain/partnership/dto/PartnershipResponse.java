package com.looky.domain.partnership.dto;

import com.looky.domain.organization.entity.OrganizationCategory;
import com.looky.domain.partnership.entity.Partnership;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PartnershipResponse {

    private final Long id;
    private final Long organizationId;
    private final String organizationName;
    private final String universityName;
    private final OrganizationCategory category;
    private final String benefit;
    private final LocalDate startsAt;
    private final LocalDate endsAt;
    private final Long storeId;
    private final String storeName;

    public PartnershipResponse(Partnership partnership) {
        this.id = partnership.getId();
        this.organizationId = partnership.getOrganization().getId();
        this.organizationName = partnership.getOrganization().getName();
        this.universityName = partnership.getOrganization().getUniversity().getName();
        this.category = partnership.getOrganization().getCategory();
        this.benefit = partnership.getBenefit();
        this.startsAt = partnership.getStartsAt();
        this.endsAt = partnership.getEndsAt();
        this.storeId = partnership.getStore().getId();
        this.storeName = partnership.getStore().getName();
    }
}
