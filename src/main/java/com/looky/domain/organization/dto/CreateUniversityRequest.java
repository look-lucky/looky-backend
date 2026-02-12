package com.looky.domain.organization.dto;

import com.looky.domain.organization.entity.University;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateUniversityRequest {

    @NotBlank(message = "대학 이름은 필수입니다.")
    private String name;

    @NotEmpty(message = "이메일 도메인은 필수입니다.")
    private List<String> emailDomains;

    public University toEntity() {
        String domains = (emailDomains != null && !emailDomains.isEmpty()) 
            ? String.join(",", emailDomains) 
            : "";
            
        return University.builder()
                .name(this.name)
                .emailDomains(domains)
                .build();
    }
}
