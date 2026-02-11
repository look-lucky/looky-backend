package com.looky.domain.organization.dto;

import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.OrganizationCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrganizationResponse {
    private Long id;
    private Long userId;
    private Long universityId;
    private String universityName;
    private OrganizationCategory category;
    private String name;
    private Long parentId;
    private String parentName;
    private LocalDateTime expiresAt;

    public static OrganizationResponse from(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .userId(organization.getUser().getId())
                .universityId(organization.getUniversity().getId())
                .universityName(organization.getUniversity().getName())
                .category(organization.getCategory())
                .name(organization.getName())
                .parentId(organization.getParent() != null ? organization.getParent().getId() : null)
                .parentName(organization.getParent() != null ? organization.getParent().getName() : null)
                .expiresAt(organization.getExpiresAt())
                .build();
    }
}
