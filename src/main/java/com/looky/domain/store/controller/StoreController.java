package com.looky.domain.store.controller;

import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreMood;

import com.looky.common.response.SwaggerErrorResponse;
import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.looky.domain.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Store", description = "상점 관련 API")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

        private final StoreService storeService;

        @Operation(summary = "[점주] 상점 등록", description = "새로운 상점을 등록합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "상점 등록 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "409", description = "이미 존재하는 상점 이름", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping
        public ResponseEntity<CommonResponse<Long>> createStore(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @Parameter(description = "상품 이미지 목록") @RequestPart List<MultipartFile> images,
                @RequestPart @Valid CreateStoreRequest request
        ) throws IOException {
                Long storeId = storeService.createStore(principalDetails.getUser(), request, images);
                return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(storeId));
        }

        @Operation(summary = "[점주] 상점 정보 수정", description = "상점 정보를 수정합니다. (본인 상점만 가능)")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "상점 수정 성공"),
                @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "409", description = "이미 존재하는 상점 이름", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PatchMapping("/{storeId}")
        public ResponseEntity<CommonResponse<Void>> updateStore(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @RequestPart @Valid UpdateStoreRequest request,
                @RequestPart List<MultipartFile> images
        ) throws IOException {
                storeService.updateStore(storeId, principalDetails.getUser(), request, images);
                return ResponseEntity.ok(CommonResponse.success(null));
        }

        @Operation(summary = "[점주] 상점 이미지 개별 삭제", description = "상점의 특정 이미지를 삭제합니다.")
        @DeleteMapping("/{storeId}/images/{imageId}")
        public ResponseEntity<CommonResponse<Void>> deleteStoreImage(
                @PathVariable Long storeId,
                @PathVariable Long imageId,
                @AuthenticationPrincipal PrincipalDetails principalDetails
        ) {
                storeService.deleteStoreImage(storeId, imageId, principalDetails.getUser());
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
        }

        @Operation(summary = "[점주] 상점 삭제", description = "상점을 삭제합니다. (본인 상점만 가능)")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "상점 삭제 성공"),
                @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @DeleteMapping("/{storeId}")
        public ResponseEntity<CommonResponse<Void>> deleteStore(
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        ) {
                storeService.deleteStore(storeId, principalDetails.getUser());
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
        }

        @Operation(summary = "[점주] 자신의 상점 조회", description = "자신이 등록한 모든 상점을 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "조회 성공")
        })
        @GetMapping("/my-stores")
        public ResponseEntity<CommonResponse<List<StoreResponse>>> getMyStores(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        ) {
                List<StoreResponse> response = storeService.getMyStores(principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[점주] 상점 통계 조회", description = "상점의 통계 데이터(단골 수, 쿠폰 발행/사용 수, 리뷰 수)를 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/{storeId}/stats")
        public ResponseEntity<CommonResponse<StoreStatsResponse>> getStoreStats(
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        ) {
                StoreStatsResponse response = storeService.getStoreStats(storeId, principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[점주] 상점 등록 상태 조회", description = "상점의 정보 및 메뉴 등록 여부를 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/{storeId}/registration-status")
        public ResponseEntity<CommonResponse<StoreRegistrationStatusResponse>> getStoreRegistrationStatus(
                @Parameter(description = "상점 ID") @PathVariable Long storeId
        ) {
                StoreRegistrationStatusResponse response = storeService.getStoreRegistrationStatus(storeId);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[학생] 상점 단건 조회", description = "상점 ID로 상점의 상세 정보를 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "상점 조회 성공"),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/{storeId}")
        public ResponseEntity<CommonResponse<StoreResponse>> getStore(
                        @Parameter(description = "상점 ID") @PathVariable Long storeId,
                        @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
                User user = principalDetails != null ? principalDetails.getUser() : null;
                StoreResponse response = storeService.getStore(storeId, user);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[학생] 상점 목록 조회", description = "전체 상점 목록을 페이징하여 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "상점 목록 조회 성공")
        })
        @GetMapping
        public ResponseEntity<CommonResponse<PageResponse<StoreResponse>>> getStores(
                @Parameter(description = "검색 키워드 (상점 이름)") @RequestParam(required = false) String keyword,
                @Parameter(description = "카테고리 필터 (복수 선택 가능)") @RequestParam(required = false) List<StoreCategory> categories,
                @Parameter(description = "분위기 필터 (복수 선택 가능)") @RequestParam(required = false) List<StoreMood> moods,
                @Parameter(description = "대학(상권) ID 필터") @RequestParam(required = false) Long universityId,
                @Parameter(description = "페이징 정보 (page, size, sort)") @PageableDefault(size = 10) Pageable pageable,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        ) {
                User user = principalDetails != null ? principalDetails.getUser() : null;
                PageResponse<StoreResponse> response = storeService.getStores(keyword, categories, moods, universityId, pageable, user);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[학생] 주위 상점 조회", description = "위도, 경도, 반경(km)을 기준으로 주위 상점을 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "상점 목록 조회 성공")
        })
        @GetMapping("/nearby")
        public ResponseEntity<CommonResponse<List<StoreResponse>>> getNearbyStores(
                @Parameter(description = "위도") @RequestParam Double latitude,
                @Parameter(description = "경도") @RequestParam Double longitude,
                @Parameter(description = "반경(km)") @RequestParam Double radius,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        ) {
                User user = principalDetails != null ? principalDetails.getUser() : null;
                List<StoreResponse> response = storeService.getNearbyStores(latitude, longitude, radius, user);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[학생] 상점 신고", description = "특정 상점을 신고합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "상점 신고 성공"),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "409", description = "이미 신고한 상점", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/{storeId}/reports")
        public ResponseEntity<CommonResponse<Void>> reportStore(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @RequestBody @Valid StoreReportRequest request)
        {
                storeService.reportStore(storeId, principalDetails.getUser().getId(), request);
                return ResponseEntity.ok(CommonResponse.success(null));
        }

        @Operation(summary = "[학생] 이번 주 핫한 가게 조회", description = "학생의 소속 대학에서 이번 주 찜이 가장 많이 늘어난 상점 Top 10을 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "403", description = "권한 없음 (학생 아님/대학 미소속)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/hot")
        public ResponseEntity<CommonResponse<List<HotStoreResponse>>> getHotStores(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        ) {
                List<HotStoreResponse> response = storeService.getHotStores(principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }

}