package com.looky.domain.admin.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.admin.dto.StoreClaimRejectionRequest;
import com.looky.domain.admin.dto.StoreClaimResponse;
import com.looky.domain.admin.service.StoreClaimVerificationService;
import com.looky.domain.store.entity.StoreClaimStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "StoreClaim", description = "상점 소유권 요청 심사 API")
@RestController
@RequestMapping("/api/admin/store-claims")
@RequiredArgsConstructor
public class StoreClaimVerificationController {

    private final StoreClaimVerificationService storeClaimVerificationService;

    @Operation(summary = "[관리자] 상점 소유권 요청 목록 조회", description = "상점 소유권 요청 목록을 조회합니다. status 파라미터로 상태별 조회가 가능합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<StoreClaimResponse>>> getStoreClaims(
            @Parameter(description = "요청 상태 (PENDING, APPROVED, REJECTED, CANCELED)") 
            @RequestParam(required = false) StoreClaimStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StoreClaimResponse> response = storeClaimVerificationService.getStoreClaims(status, pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[관리자] 상점 소유권 요청 승인", description = "상점 소유권 요청을 승인합니다.")
    @PostMapping("/{claimId}/approve")
    public ResponseEntity<CommonResponse<Void>> approve(@PathVariable Long claimId) {
        storeClaimVerificationService.approve(claimId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 상점 소유권 요청 반려", description = "상점 소유권 요청을 반려합니다.")
    @PostMapping("/{claimId}/reject")
    public ResponseEntity<CommonResponse<Void>> reject(
            @PathVariable Long claimId,
            @RequestBody StoreClaimRejectionRequest request) {
        storeClaimVerificationService.reject(claimId, request.getReason());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 상점 소유권 요청 관리자 메모 등록 및 수정", description = "상점 소유권 요청에 관리자 메모를 남깁니다.")
    @PatchMapping("/{claimId}/memo")
    public ResponseEntity<CommonResponse<Void>> updateMemo(
            @PathVariable Long claimId,
            @RequestBody String memo) {
        storeClaimVerificationService.updateMemo(claimId, memo);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
