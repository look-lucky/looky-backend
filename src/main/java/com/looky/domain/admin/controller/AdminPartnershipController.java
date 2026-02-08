package com.looky.domain.admin.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.admin.service.AdminPartnershipService;
import com.looky.domain.partnership.dto.CreatePartnershipRequest;
import com.looky.domain.partnership.dto.PartnershipResponse;
import com.looky.domain.partnership.dto.UpdatePartnershipRequest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Admin Partnership", description = "관리자 제휴 관련 API (일반/엑셀)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPartnershipController {

    private final AdminPartnershipService adminPartnershipService;

    @Operation(summary = "[관리자] 제휴 단건 등록", description = "특정 대학의 특정 조직에 제휴를 단건으로 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "제휴 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 또는 조직 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 등록된 제휴", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/universities/{universityId}/organizations/{organizationId}/partnerships")
    public ResponseEntity<CommonResponse<Long>> createPartnership(
            @Parameter(description = "대학 ID") @PathVariable Long universityId,
            @Parameter(description = "조직 ID") @PathVariable Long organizationId,
            @RequestBody @Valid CreatePartnershipRequest request) {
        Long partnershipId = adminPartnershipService.createPartnership(universityId, organizationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(partnershipId));
    }

    @Operation(summary = "[관리자] 제휴 혜택 수정", description = "제휴 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제휴 혜택 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "제휴 정보 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/partnerships/{partnershipId}")
    public ResponseEntity<CommonResponse<Void>> updatePartnershipBenefit(
            @Parameter(description = "제휴 ID") @PathVariable Long partnershipId,
            @RequestBody @Valid UpdatePartnershipRequest request) {
        adminPartnershipService.updatePartnershipBenefit(partnershipId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 제휴 삭제", description = "제휴를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "제휴 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "제휴 정보 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/partnerships/{partnershipId}")
    public ResponseEntity<CommonResponse<Void>> deletePartnership(
            @Parameter(description = "제휴 ID") @PathVariable Long partnershipId) {
        adminPartnershipService.deletePartnership(partnershipId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @Operation(summary = "대학별 제휴 목록 조회", description = "특정 대학의 모든 제휴를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/universities/{universityId}/partnerships")
    public ResponseEntity<CommonResponse<List<PartnershipResponse>>> getPartnershipsByUniversity(
            @Parameter(description = "대학 ID") @PathVariable Long universityId) {
        return ResponseEntity
                .ok(CommonResponse.success(adminPartnershipService.getPartnershipsByUniversity(universityId)));
    }

    @Operation(summary = "조직별 제휴 목록 조회", description = "특정 대학의 특정 조직의 모든 제휴를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/universities/{universityId}/organizations/{organizationId}/partnerships")
    public ResponseEntity<CommonResponse<List<PartnershipResponse>>> getPartnershipsByOrganization(
            @Parameter(description = "대학 ID") @PathVariable Long universityId,
            @Parameter(description = "조직 ID") @PathVariable Long organizationId) {
        return ResponseEntity.ok(CommonResponse
                .success(adminPartnershipService.getPartnershipsByOrganization(universityId, organizationId)));
    }

    @Operation(summary = "[관리자] 제휴 등록 템플릿 다운로드", description = "특정 대학의 상점 리스트가 포함된 엑셀 템플릿을 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping(value = "/partnerships/template", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportPartnershipTemplate(
            @Parameter(description = "대상 대학 ID") @RequestParam Long universityId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) throws IOException {
        var result = adminPartnershipService.exportPartnershipTemplate(universityId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .body(result.content());
    }

    @Operation(summary = "[학생회/관리자] 제휴 엑셀로 등록", description = "엑셀 파일을 업로드하여 제휴 정보를 일괄 등록/수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "데이터 검증 실패 (에러 메시지 포함)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping(value = "/partnerships/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Void>> uploadPartnershipData(
            @Parameter(description = "엑셀 파일") @RequestPart("file") MultipartFile file,
            @Parameter(description = "대상 조직 ID (관리자용)") @RequestParam(required = false) Long organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        adminPartnershipService.importPartnershipData(file, principalDetails.getUser(), organizationId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
