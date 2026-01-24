package com.looky.domain.organization.dto;

import com.looky.domain.organization.entity.OrganizationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateOrganizationRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private OrganizationCategory category;

    @NotBlank(message = "소속 이름은 필수입니다.")
    private String name;

    private Long parentId;

    private LocalDateTime expiresAt;
}
