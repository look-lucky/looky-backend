package com.looky.domain.partnership.dto;

import com.looky.domain.organization.entity.OrganizationCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PartnershipInfo {
    private Long organizationId;
    private OrganizationCategory category;
    private String name;

    public static PartnershipInfo of(Long organizationId, OrganizationCategory category, String name) {
        return new PartnershipInfo(organizationId, category, name);
    }
}
