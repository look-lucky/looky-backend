package com.looky.domain.organization.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.organization.dto.CreateUniversityRequest;
import com.looky.domain.organization.dto.UpdateUniversityRequest;
import com.looky.domain.organization.service.UniversityService;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin University", description = "관리자 대학 관리 API")
@RestController
@RequestMapping("/api/admin/universities")
@RequiredArgsConstructor
public class AdminUniversityController {

    private final UniversityService universityService;

    @Operation(summary = "[관리자] 대학 등록", description = "새로운 대학을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createUniversity(
            @RequestBody @Valid CreateUniversityRequest request) {
        Long universityId = universityService.createUniversityForAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(universityId));
    }

    @Operation(summary = "[관리자] 대학 수정", description = "대학 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "대학 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/{universityId}")
    public ResponseEntity<CommonResponse<Void>> updateUniversity(
            @Parameter(description = "대학 ID") @PathVariable Long universityId,
            @RequestBody @Valid UpdateUniversityRequest request) {
        universityService.updateUniversityForAdmin(universityId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 대학 삭제", description = "대학을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "소속이 있어 삭제 불가", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "대학 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/{universityId}")
    public ResponseEntity<CommonResponse<Void>> deleteUniversity(
            @Parameter(description = "대학 ID") @PathVariable Long universityId) {
        universityService.deleteUniversityForAdmin(universityId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
