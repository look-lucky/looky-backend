package com.looky.domain.store.dto;

import com.looky.domain.organization.entity.OrganizationCategory;
import com.looky.domain.store.entity.StoreOrganization;
import lombok.Getter;

@Getter
public class PartnershipResponse {

    private final Long id;
    private final Long organizationId;
    private final String organizationName;
    private final String universityName;
    private final OrganizationCategory category;
    private final String benefit;

    public PartnershipResponse(StoreOrganization storeOrganization) {
        this.id = storeOrganization.getId();
        this.organizationId = storeOrganization.getOrganization().getId();
        this.organizationName = storeOrganization.getOrganization().getName();
        this.universityName = storeOrganization.getOrganization().getUniversity().getName();
        this.category = storeOrganization.getOrganization().getCategory();
        this.benefit = storeOrganization.getBenefit();
    }
}
