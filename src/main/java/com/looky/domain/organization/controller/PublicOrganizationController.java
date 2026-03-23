package com.looky.domain.organization.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.organization.dto.OrganizationResponse;
import com.looky.domain.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public Organization", description = "소속 공개 조회 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicOrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "[공통] 특정 대학의 소속 목록 조회", description = "대학의 모든 소속을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "대학 없음")
    })
    @GetMapping("/universities/{universityId}/organizations")
    public ResponseEntity<CommonResponse<List<OrganizationResponse>>> getOrganizations(
            @Parameter(description = "대학 ID") @PathVariable Long universityId) {
        List<OrganizationResponse> responses = organizationService.getOrganizationsForAll(universityId);
        return ResponseEntity.ok(CommonResponse.success(responses));
    }

    @Operation(summary = "[공통] 특정 단과대학의 학과 목록 조회", description = "단과대학에 속한 학과 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "단과대학 없음")
    })
    @GetMapping("/organizations/{collegeId}/departments")
    public ResponseEntity<CommonResponse<List<OrganizationResponse>>> getDepartmentsByCollege(
            @Parameter(description = "단과대학 ID") @PathVariable Long collegeId) {
        List<OrganizationResponse> responses = organizationService.getDepartmentsByCollegeForAll(collegeId);
        return ResponseEntity.ok(CommonResponse.success(responses));
    }
}
