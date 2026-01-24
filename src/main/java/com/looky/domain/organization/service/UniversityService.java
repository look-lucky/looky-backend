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

        university.update(request.getName(), request.getEmailDomain());
    }

    @Transactional
    public void deleteUniversity(Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

        universityRepository.delete(university);
    }
}
