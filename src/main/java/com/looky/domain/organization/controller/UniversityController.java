package com.looky.domain.organization.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.organization.dto.CreateUniversityRequest;
import com.looky.domain.organization.dto.UpdateUniversityRequest;
import com.looky.domain.organization.service.UniversityService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @deprecated 하위 호환용. 신규 클라이언트는 아래 컨트롤러를 사용하세요.
 * - 조회: {@link PublicUniversityController}
 * - 관리: {@link AdminUniversityController}
 */
@Deprecated
@Hidden
@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createUniversity(
            @RequestBody @Valid CreateUniversityRequest request) {
        Long universityId = universityService.createUniversityForAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(universityId));
    }

    @PatchMapping("/{universityId}")
    public ResponseEntity<CommonResponse<Void>> updateUniversity(
            @PathVariable Long universityId,
            @RequestBody @Valid UpdateUniversityRequest request) {
        universityService.updateUniversityForAdmin(universityId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/{universityId}")
    public ResponseEntity<CommonResponse<Void>> deleteUniversity(
            @PathVariable Long universityId) {
        universityService.deleteUniversityForAdmin(universityId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
