package com.looky.domain.organization.dto;

import com.looky.domain.organization.entity.University;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateUniversityRequest {

    @NotBlank(message = "대학 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "이메일 도메인은 필수입니다.")
    private String emailDomain;

    public University toEntity() {
        return University.builder()
                .name(this.name)
                .emailDomain(this.emailDomain)
                .build();
    }
}
