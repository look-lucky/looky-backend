package com.looky.domain.organization.dto;

import com.looky.domain.organization.entity.University;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

@Getter
@Builder
public class UniversityResponse {
    private Long id;
    private String name;
    private List<String> emailDomains;

    public static UniversityResponse from(University university) {
        List<String> domains = (university.getEmailDomains() != null && !university.getEmailDomains().isEmpty())
            ? Arrays.asList(university.getEmailDomains().split(","))
            : Collections.emptyList();

        return UniversityResponse.builder()
                .id(university.getId())
                .name(university.getName())
                .emailDomains(domains)
                .build();
    }
}
