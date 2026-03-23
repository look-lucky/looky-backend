package com.looky.domain.storeclaim.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.storeclaim.dto.AdminStoreClaimResponse;
import com.looky.domain.storeclaim.dto.StoreClaimRejectionRequest;
import com.looky.domain.storeclaim.entity.StoreClaimStatus;
import com.looky.domain.storeclaim.service.AdminStoreClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin StoreClaim", description = "관리자 상점 소유권 요청 심사 API")
@RestController
@RequestMapping("/api/admin/store-claims")
@RequiredArgsConstructor
public class AdminStoreClaimController {

    private final AdminStoreClaimService adminStoreClaimService;

    @Operation(summary = "[관리자] 상점 소유권 요청 목록 조회", description = "상점 소유권 요청 목록을 조회합니다. status 파라미터로 상태별 조회가 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<Page<AdminStoreClaimResponse>>> getStoreClaims(
            @Parameter(description = "요청 상태 (PENDING, APPROVED, REJECTED, CANCELED)")
            @RequestParam(required = false) StoreClaimStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminStoreClaimResponse> response = adminStoreClaimService.getStoreClaimsForAdmin(status, pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[관리자] 상점 소유권 요청 승인", description = "상점 소유권 요청을 승인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "대기 중인 요청이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/{claimId}/approve")
    public ResponseEntity<CommonResponse<Void>> approve(
            @Parameter(description = "소유 요청 ID") @PathVariable Long claimId) {
        adminStoreClaimService.approveForAdmin(claimId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 상점 소유권 요청 반려", description = "상점 소유권 요청을 반려합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "반려 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "대기 중인 요청이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/{claimId}/reject")
    public ResponseEntity<CommonResponse<Void>> reject(
            @Parameter(description = "소유 요청 ID") @PathVariable Long claimId,
            @RequestBody StoreClaimRejectionRequest request) {
        adminStoreClaimService.rejectForAdmin(claimId, request.getReason());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 상점 소유권 요청 관리자 메모 등록 및 수정", description = "상점 소유권 요청에 관리자 메모를 남깁니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메모 저장 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/{claimId}/memo")
    public ResponseEntity<CommonResponse<Void>> updateMemo(
            @Parameter(description = "소유 요청 ID") @PathVariable Long claimId,
            @RequestBody String memo) {
        adminStoreClaimService.updateMemoForAdmin(claimId, memo);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
