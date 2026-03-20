package com.looky.domain.store.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.store.dto.*;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreMood;
import com.looky.domain.store.entity.StoreStatus;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Store", description = "학생 상점 조회 API")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentStoreController {

    private final StoreService storeService;

    @Operation(summary = "[학생] 상점 단건 조회", description = "상점 ID로 상세 정보와 나의 파트너십 혜택을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<CommonResponse<StudentStoreResponse>> getStore(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        StudentStoreResponse response = storeService.getStoreForStudent(storeId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 상점 목록 조회", description = "전체 상점 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/stores")
    public ResponseEntity<CommonResponse<PageResponse<StudentStoreResponse>>> getStores(
            @Parameter(description = "검색 키워드 (상점 이름)") @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 필터 (복수 선택 가능)") @RequestParam(required = false) List<StoreCategory> categories,
            @Parameter(description = "분위기 필터 (복수 선택 가능)") @RequestParam(required = false) List<StoreMood> moods,
            @Parameter(description = "대학(상권) ID 필터") @RequestParam(required = false) Long universityId,
            @Parameter(description = "제휴 업체 보유 여부 필터") @RequestParam(required = false) Boolean hasPartnership,
            @Parameter(description = "상점 상태 필터") @RequestParam(required = false) StoreStatus storeStatus,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 10) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        PageResponse<StudentStoreResponse> response = storeService.getStoresForStudent(keyword, categories, moods, universityId, hasPartnership, storeStatus, pageable, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 주변 상점 조회", description = "위도, 경도, 반경(km)을 기준으로 주변 상점을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/stores/nearby")
    public ResponseEntity<CommonResponse<List<StudentStoreResponse>>> getNearbyStores(
            @Parameter(description = "위도") @RequestParam Double latitude,
            @Parameter(description = "경도") @RequestParam Double longitude,
            @Parameter(description = "반경(km)") @RequestParam Double radius,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<StudentStoreResponse> response = storeService.getNearbyStoresForStudent(latitude, longitude, radius, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 특정 위치 상점 목록 조회", description = "위도, 경도가 일치하는 상점 목록을 조회합니다. (같은 건물/위치)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/stores/location")
    public ResponseEntity<CommonResponse<List<StudentStoreResponse>>> getStoresByLocation(
            @Parameter(description = "위도") @RequestParam Double latitude,
            @Parameter(description = "경도") @RequestParam Double longitude,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<StudentStoreResponse> response = storeService.getStoresByLocationForStudent(latitude, longitude, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 지도용 상점 전체 조회", description = "지도를 위한 상점 전체 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/stores/map")
    public ResponseEntity<CommonResponse<List<StudentStoreMapResponse>>> getStoreMap(
            @Parameter(description = "대학(상권) ID 필터") @RequestParam(required = false) Long universityId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<StudentStoreMapResponse> response = storeService.getStoreMapForStudent(universityId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 이번 주 핫한 가게 조회", description = "소속 대학에서 이번 주 찜이 가장 많이 늘어난 상점 Top 10을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (대학 미소속)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/hot")
    public ResponseEntity<CommonResponse<List<HotStoreResponse>>> getHotStores(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<HotStoreResponse> response = storeService.getHotStoresForStudent(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 상점 신고", description = "특정 상점을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 신고한 상점", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/stores/{storeId}/reports")
    public ResponseEntity<CommonResponse<Void>> reportStore(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @RequestBody @Valid StoreReportRequest request
    ) {
        storeService.reportStoreForStudent(storeId, principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
