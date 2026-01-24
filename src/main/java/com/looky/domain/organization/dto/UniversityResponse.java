package com.looky.domain.organization.dto;

import com.looky.domain.organization.entity.University;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UniversityResponse {
    private Long id;
    private String name;
    private String emailDomain;

    public static UniversityResponse from(University university) {
        return UniversityResponse.builder()
                .id(university.getId())
                .name(university.getName())
                .emailDomain(university.getEmailDomain())
                .build();
    }
}
