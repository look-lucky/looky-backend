package com.looky.domain.organization.dto;

import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.OrganizationCategory;
import com.looky.domain.organization.entity.University;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateOrganizationRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private OrganizationCategory category;

    @NotBlank(message = "소속 이름은 필수입니다.")
    private String name;

    private Long parentId;

    private LocalDateTime expiresAt;

    public Organization toEntity(University university, Organization parent) {
        return Organization.builder()
                .university(university)
                .category(this.category)
                .name(this.name)
                .expiresAt(this.expiresAt)
                .parent(parent)
                .build();
    }
}
