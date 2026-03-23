package com.looky.domain.organization.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.organization.dto.CreateOrganizationRequest;
import com.looky.domain.organization.dto.UpdateOrganizationRequest;
import com.looky.domain.organization.service.OrganizationService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Council Organization", description = "학생회 소속 관리 API")
@RestController
@RequestMapping("/api/council")
@RequiredArgsConstructor
public class CouncilOrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "[학생회] 특정 대학에 소속 등록", description = "자신의 대학에 새로운 소속(단과대, 학과 등)을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "403", description = "학생회 권한 필요 또는 타 대학 접근", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "대학 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "소속 이름 중복", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/universities/{universityId}/organizations")
    public ResponseEntity<CommonResponse<Long>> createOrganization(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "대학 ID") @PathVariable Long universityId,
            @RequestBody @Valid CreateOrganizationRequest request) {
        Long organizationId = organizationService.createOrganizationForCouncil(principalDetails.getUser(), universityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(organizationId));
    }

    @Operation(summary = "[학생회] 소속 수정", description = "본인이 생성한 소속 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "학생회 권한 필요 또는 타인 소속 접근", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "소속 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "소속 이름 중복", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/organizations/{organizationId}")
    public ResponseEntity<CommonResponse<Void>> updateOrganization(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "소속 ID") @PathVariable Long organizationId,
            @RequestBody @Valid UpdateOrganizationRequest request) {
        organizationService.updateOrganizationForCouncil(organizationId, principalDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[학생회] 소속 삭제", description = "본인이 생성한 소속을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "하위 조직 존재", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "학생회 권한 필요 또는 타인 소속 접근", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "소속 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/organizations/{organizationId}")
    public ResponseEntity<CommonResponse<Void>> deleteOrganization(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "소속 ID") @PathVariable Long organizationId) {
        organizationService.deleteOrganizationForCouncil(organizationId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
