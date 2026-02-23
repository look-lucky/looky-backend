package com.looky.domain.partnership.dto;

import com.looky.domain.organization.entity.OrganizationCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PartnershipInfo {
    private OrganizationCategory category;
    private String name;

    public static PartnershipInfo of(OrganizationCategory category, String name) {
        return new PartnershipInfo(category, name);
    }
}
