package com.looky.domain.store.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.store.dto.BizVerificationRequest;
import com.looky.domain.store.dto.BizVerificationResponse;
import com.looky.domain.store.dto.StoreResponse;
import com.looky.domain.store.dto.StoreClaimRequest;
import com.looky.domain.store.service.StoreClaimService;
import com.looky.security.details.PrincipalDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "StoreClaim", description = "상점 소유권 등록 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreClaimController {

    private final StoreClaimService storeClaimService;

    @Operation(summary = "[점주] 미등록 상점 검색", description = "시스템에 등록된 미등록 상점을 이름 또는 주소로 검색합니다.")
    @GetMapping("/store-claims/search")
    public ResponseEntity<CommonResponse<List<StoreResponse>>> searchUnclaimedStores(
            @RequestParam String keyword
    ) {
        List<StoreResponse> response = storeClaimService.searchUnclaimedStores(keyword);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 사업자등록번호 유효성 검증", description = "사업자등록번호의 유효성을 검증합니다.")
    @PostMapping("/biz-reg-no/verify")
    public ResponseEntity<CommonResponse<BizVerificationResponse>> verifyBizRegNo(@RequestBody @Valid BizVerificationRequest request) {
        BizVerificationResponse response = storeClaimService.verifyBizRegNo(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }


    @Operation(summary = "[점주] 상점 소유 요청 등록", description = "사장님이 상점에 대해 소유를 요청하여 심사 대상이 됩니다.")
    @PostMapping("/store-claims")
    public ResponseEntity<CommonResponse<Long>> createStoreClaims(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestPart @Valid StoreClaimRequest request,
            @Parameter(description = "사업자등록증 이미지") @RequestPart MultipartFile image
    ) throws IOException {
        Long storeClaimId = storeClaimService.createStoreClaims(principalDetails.getUser(), request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(storeClaimId));
    }

}
