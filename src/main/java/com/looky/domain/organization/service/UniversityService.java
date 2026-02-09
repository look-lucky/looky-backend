package com.looky.domain.organization.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.dto.CreateUniversityRequest;
import com.looky.domain.organization.dto.UniversityResponse;
import com.looky.domain.organization.dto.UpdateUniversityRequest;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityService {

    private final UniversityRepository universityRepository;

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
        if (request.getEmailDomain().isPresent()) {
             String emailDomain = request.getEmailDomain().get();
             if (emailDomain == null || emailDomain.isBlank()) {
                 throw new CustomException(ErrorCode.BAD_REQUEST, "이메일 도메인은 필수입니다.");
             }
        }

        university.update(
            request.getName().orElse(university.getName()),
            request.getEmailDomain().orElse(university.getEmailDomain())
        );
    }

    @Transactional
    public void deleteUniversity(Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

        universityRepository.delete(university);
    }
}
