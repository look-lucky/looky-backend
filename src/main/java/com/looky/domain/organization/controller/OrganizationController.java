package com.looky.domain.organization.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.organization.dto.CreateOrganizationRequest;
import com.looky.domain.organization.dto.UpdateOrganizationRequest;
import com.looky.domain.organization.service.OrganizationService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @deprecated 하위 호환용. 신규 클라이언트는 아래 컨트롤러를 사용하세요.
 * - 조회: {@link PublicOrganizationController}
 * - 관리자: {@link AdminOrganizationController}
 * - 학생회: {@link CouncilOrganizationController}
 * - 학생: {@link StudentOrganizationController}
 */
@Deprecated
@Hidden
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/universities/{universityId}/organizations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNCIL')")
    public ResponseEntity<CommonResponse<Long>> createOrganization(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long universityId,
            @RequestBody @Valid CreateOrganizationRequest request) {
        Long organizationId = organizationService.createOrganizationForAdmin(universityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(organizationId));
    }

    @PatchMapping("/organizations/{organizationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNCIL')")
    public ResponseEntity<CommonResponse<Void>> updateOrganization(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long organizationId,
            @RequestBody @Valid UpdateOrganizationRequest request) {
        organizationService.updateOrganizationForAdmin(organizationId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/organizations/{organizationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNCIL')")
    public ResponseEntity<CommonResponse<Void>> deleteOrganization(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long organizationId) {
        organizationService.deleteOrganizationForAdmin(organizationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @PostMapping("/organizations/{organizationId}/membership")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CommonResponse<Void>> joinOrganization(
            @PathVariable Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        organizationService.joinOrganizationForStudent(organizationId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/organizations/{organizationId}/membership")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CommonResponse<Void>> leaveOrganization(
            @PathVariable Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        organizationService.leaveOrganizationForStudent(organizationId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @PatchMapping("/organizations/{organizationId}/membership")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CommonResponse<Void>> changeOrganization(
            @PathVariable Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        organizationService.changeOrganizationForStudent(organizationId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
