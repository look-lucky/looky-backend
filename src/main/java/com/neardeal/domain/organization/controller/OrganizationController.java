package com.neardeal.domain.organization.controller;

import com.neardeal.common.response.CommonResponse;
import com.neardeal.domain.organization.dto.OrganizationResponse;
import com.neardeal.domain.organization.dto.CreateOrganizationRequest;
import com.neardeal.domain.organization.dto.UpdateOrganizationRequest;
import com.neardeal.domain.organization.service.OrganizationService;
import com.neardeal.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Organization", description = "대학 소속 관리 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    // --- 공통 ---

    @Operation(summary = "[공통] 특정 대학의 소속 목록 조회", description = "대학의 모든 소속을 조회합니다.")
    @GetMapping("/universities/{universityId}/organizations")
    public ResponseEntity<CommonResponse<List<OrganizationResponse>>> getOrganizations(
            @PathVariable Long universityId
    )
    {
        List<OrganizationResponse> responses = organizationService.getOrganizations(universityId);
        return ResponseEntity.ok(CommonResponse.success(responses));
    }

    // --- 관리자 ---

    @Operation(summary = "[관리자] 특정 대학에 소속 등록", description = "대학에 새로운 소속(단과대, 학과 등)을 등록합니다.")
    @PostMapping("/universities/{universityId}/organizations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Long>> createOrganization(
            @PathVariable Long universityId,
            @RequestBody @Valid CreateOrganizationRequest request
    )
    {
        Long organizationId = organizationService.createOrganization(universityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(organizationId));
    }

    @Operation(summary = "[관리자] 소속 수정", description = "소속 정보를 수정합니다.")
    @PatchMapping("/organizations/{organizationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> updateOrganization(
            @PathVariable Long organizationId,
            @RequestBody @Valid UpdateOrganizationRequest request
    )
    {
        organizationService.updateOrganization(organizationId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 소속 삭제", description = "소속을 삭제합니다.")
    @DeleteMapping("/organizations/{organizationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> deleteOrganization(
            @PathVariable Long organizationId
    )
    {
        organizationService.deleteOrganization(organizationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }


    // --- 학생 ---

    @Operation(summary = "[학생] 소속 가입", description = "학생이 특정 소속에 가입합니다.")
    @PostMapping("/organizations/{organizationId}/membership")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<Void>> joinOrganization(
            @PathVariable Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    )
    {
        organizationService.joinOrganization(organizationId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[학생] 소속 탈퇴", description = "학생이 특정 소속에서 탈퇴합니다.")
    @DeleteMapping("/organizations/{organizationId}/membership")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<Void>> leaveOrganization(
            @PathVariable Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    )
    {
        organizationService.leaveOrganization(organizationId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
