package com.looky.domain.organization.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.dto.CreateUniversityRequest;
import com.looky.domain.organization.dto.UniversityResponse;
import com.looky.domain.organization.dto.UpdateUniversityRequest;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.organization.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Long createUniversity(CreateUniversityRequest request) {
        University university = request.toEntity();
        University savedUniversity = universityRepository.save(university);
        return savedUniversity.getId();
    }

    public List<UniversityResponse> getUniversities() {
        return universityRepository.findAll().stream()
                .map(UniversityResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUniversity(Long universityId, UpdateUniversityRequest request) {

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

        if (request.getName().isPresent()) {
            String name = request.getName().get();
            if (name == null || name.isBlank()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "대학 이름은 필수입니다.");
            }
        }
        if (request.getEmailDomains().isPresent()) {
             List<String> emailDomains = request.getEmailDomains().get();
             if (emailDomains == null || emailDomains.isEmpty()) {
                 throw new CustomException(ErrorCode.BAD_REQUEST, "이메일 도메인은 필수입니다.");
             }
        }

        String emailDomains = null;
        if (request.getEmailDomains().isPresent()) {
            List<String> domains = request.getEmailDomains().orElse(Collections.emptyList());
            if (!domains.isEmpty()) {
                emailDomains = String.join(",", domains);
            }
        }

        university.update(
            request.getName().orElse(university.getName()),
            emailDomains != null ? emailDomains : university.getEmailDomains()
        );
    }

    @Transactional
    public void deleteUniversity(Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

        if (organizationRepository.existsByUniversityId(universityId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "등록된 소속(단과대학, 학과 등)이 있어 삭제할 수 없습니다.");
        }

        universityRepository.delete(university);
    }
}
