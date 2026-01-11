package com.neardeal.domain.organization.controller;

import com.neardeal.common.response.CommonResponse;
import com.neardeal.domain.organization.dto.CreateUniversityRequest;
import com.neardeal.domain.organization.dto.UniversityResponse;
import com.neardeal.domain.organization.dto.UpdateUniversityRequest;
import com.neardeal.domain.organization.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "University", description = "대학 관리 API")
@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    // --- 공통 ---

    @Operation(summary = "[공통] 대학 목록 조회", description = "전체 대학 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<List<UniversityResponse>>> getUniversities() {
        List<UniversityResponse> responses = universityService.getUniversities();
        return ResponseEntity.ok(CommonResponse.success(responses));
    }

    // --- 관리자 ---

    @Operation(summary = "[관리자] 대학 등록", description = "새로운 대학을 등록합니다.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Long>> createUniversity(
            @RequestBody @Valid CreateUniversityRequest request
    )
    {
        Long universityId = universityService.createUniversity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(universityId));
    }

    @Operation(summary = "[관리자] 대학 수정", description = "대학 정보를 수정합니다.")
    @PatchMapping("/{universityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> updateUniversity(
            @PathVariable Long universityId,
            @RequestBody @Valid UpdateUniversityRequest request
    )
    {
        universityService.updateUniversity(universityId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 대학 삭제", description = "대학을 삭제합니다.")
    @DeleteMapping("/{universityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> deleteUniversity(
            @PathVariable Long universityId
    )
    {
        universityService.deleteUniversity(universityId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}