package com.looky.domain.store.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.store.dto.*;
import com.looky.domain.store.service.StoreService;
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

import java.util.List;

@Tag(name = "Owner Store", description = "점주 상점 관리 API")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerStoreController {

    private final StoreService storeService;

    @Operation(summary = "[점주] 상점 등록", description = "새로운 상점을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상점 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 상점 (상점명 + 지점명 기준)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/stores")
    public ResponseEntity<CommonResponse<Long>> createStore(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid StoreCreateRequest request
    ) {
        Long storeId = storeService.createStoreForOwner(principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(storeId));
    }

    @Operation(summary = "[점주] 상점 정보 수정", description = "상점 정보를 수정합니다. (본인 상점만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상점 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 상점 (상점명 + 지점명 기준)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/stores/{storeId}")
    public ResponseEntity<CommonResponse<Void>> updateStore(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @RequestBody @Valid StoreUpdateRequest request
    ) {
        storeService.updateStoreForOwner(storeId, principalDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 상점 삭제", description = "상점을 삭제합니다. (본인 상점만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "상점 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<CommonResponse<Void>> deleteStore(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        storeService.deleteStoreForOwner(storeId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 자신의 상점 목록 조회", description = "자신이 등록한 모든 상점을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/stores")
    public ResponseEntity<CommonResponse<List<OwnerStoreResponse>>> getMyStores(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<OwnerStoreResponse> response = storeService.getMyStoresForOwner(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 상점 통계 조회", description = "상점의 통계 데이터(단골 수, 쿠폰 발행/사용 수, 리뷰 수)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}/stats")
    public ResponseEntity<CommonResponse<StoreStatsResponse>> getStoreStats(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        StoreStatsResponse response = storeService.getStoreStatsForOwner(storeId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 상점 등록 상태 조회", description = "상점의 정보 및 메뉴 등록 여부를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}/registration-status")
    public ResponseEntity<CommonResponse<StoreRegistrationStatusResponse>> getStoreRegistrationStatus(
            @Parameter(description = "상점 ID") @PathVariable Long storeId
    ) {
        StoreRegistrationStatusResponse response = storeService.getStoreRegistrationStatusForOwner(storeId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
