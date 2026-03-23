package com.looky.domain.storeclaim.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.store.dto.StoreResponse;
import com.looky.domain.storeclaim.dto.BizVerificationRequest;
import com.looky.domain.storeclaim.dto.BizVerificationResponse;
import com.looky.domain.storeclaim.dto.MyStoreClaimResponse;
import com.looky.domain.storeclaim.dto.StoreClaimRequest;
import com.looky.domain.storeclaim.service.StoreClaimService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Owner StoreClaim", description = "점주 상점 소유권 등록 API")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerStoreClaimController {

    private final StoreClaimService storeClaimService;

    @Operation(summary = "[점주] 미등록 상점 검색", description = "시스템에 등록된 미등록 상점을 이름 또는 주소로 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/store-claims/search")
    public ResponseEntity<CommonResponse<List<StoreResponse>>> searchUnclaimedStores(
            @RequestParam String keyword
    ) {
        List<StoreResponse> response = storeClaimService.searchUnclaimedStoresForOwner(keyword);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 사업자등록번호 유효성 검증", description = "사업자등록번호의 유효성을 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검증 성공"),
            @ApiResponse(responseCode = "400", description = "사업자 정보 불일치", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "휴업 또는 폐업 사업자", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/biz-reg-no/verify")
    public ResponseEntity<CommonResponse<BizVerificationResponse>> verifyBizRegNo(
            @RequestBody @Valid BizVerificationRequest request
    ) {
        BizVerificationResponse response = storeClaimService.verifyBizRegNoForOwner(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 상점 소유 요청 등록", description = "점주가 상점에 대해 소유를 요청하여 심사 대상이 됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "소유 요청 등록 성공"),
            @ApiResponse(responseCode = "403", description = "점주 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 대기 중인 요청 존재", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/store-claims")
    public ResponseEntity<CommonResponse<Long>> createStoreClaims(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid StoreClaimRequest request
    ) {
        Long storeClaimId = storeClaimService.createStoreClaimsForOwner(principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(storeClaimId));
    }

    @Operation(summary = "[점주] 내 상점 소유 요청 목록 조회", description = "점주가 자신이 신청한 상점 소유 요청 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "점주 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/store-claims")
    public ResponseEntity<CommonResponse<List<MyStoreClaimResponse>>> getMyStoreClaims(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<MyStoreClaimResponse> response = storeClaimService.getMyStoreClaimsForOwner(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
