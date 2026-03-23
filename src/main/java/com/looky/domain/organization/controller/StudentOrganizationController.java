package com.looky.domain.organization.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.organization.service.OrganizationService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Student Organization", description = "학생 소속 관리 API")
@RestController
@RequestMapping("/api/student/organizations")
@RequiredArgsConstructor
public class StudentOrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "[학생] 소속 가입", description = "학생이 특정 소속에 가입합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "학생 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "소속 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 가입된 소속", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/{organizationId}/membership")
    public ResponseEntity<CommonResponse<Void>> joinOrganization(
            @Parameter(description = "소속 ID") @PathVariable Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        organizationService.joinOrganizationForStudent(organizationId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[학생] 소속 탈퇴", description = "학생이 특정 소속에서 탈퇴합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "403", description = "학생 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "소속 없음 또는 미가입", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/{organizationId}/membership")
    public ResponseEntity<CommonResponse<Void>> leaveOrganization(
            @Parameter(description = "소속 ID") @PathVariable Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        organizationService.leaveOrganizationForStudent(organizationId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @Operation(summary = "[학생] 소속 변경", description = "학생이 소속을 변경합니다. 기존 동종 소속은 자동 탈퇴됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "학생 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "소속 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/{organizationId}/membership")
    public ResponseEntity<CommonResponse<Void>> changeOrganization(
            @Parameter(description = "소속 ID") @PathVariable Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        organizationService.changeOrganizationForStudent(organizationId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
