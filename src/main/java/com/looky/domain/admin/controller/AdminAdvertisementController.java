package com.looky.domain.admin.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.advertisement.dto.AdminAdvertisementResponse;
import com.looky.domain.advertisement.dto.CreateAdvertisementRequest;
import com.looky.domain.advertisement.dto.UpdateAdvertisementRequest;
import com.looky.domain.advertisement.entity.AdvertisementStatus;
import com.looky.domain.advertisement.entity.AdvertisementType;
import com.looky.domain.advertisement.service.AdvertisementService;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Advertisement", description = "관리자 광고 관리 API")
@RestController
@RequestMapping("/api/admin/advertisements")
@RequiredArgsConstructor
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "[관리자] 광고 등록", description = "새로운 광고를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "광고 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createAdvertisement(
            @RequestBody @Valid CreateAdvertisementRequest request) {
        Long advertisementId = advertisementService.createAdvertisement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(advertisementId));
    }

    @Operation(summary = "[관리자] 광고 목록 조회", description = "광고 목록을 조회합니다. 타입 및 상태 필터를 지원합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<AdminAdvertisementResponse>>> getAdvertisements(
            @Parameter(description = "광고 타입 필터 (POPUP / BANNER / FLOATING)") @RequestParam(required = false) AdvertisementType type,
            @Parameter(description = "광고 상태 필터 (SCHEDULED / ACTIVE / INACTIVE / ENDED)") @RequestParam(required = false) AdvertisementStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<AdminAdvertisementResponse> response = advertisementService.getAdvertisements(type, status, pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[관리자] 광고 수정", description = "광고 정보 및 상태를 수정합니다. 상태는 활성화/비활성화만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "광고 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "광고 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/{advertisementId}")
    public ResponseEntity<CommonResponse<Void>> updateAdvertisement(
            @Parameter(description = "광고 ID") @PathVariable Long advertisementId,
            @RequestBody @Valid UpdateAdvertisementRequest request) {
        advertisementService.updateAdvertisement(advertisementId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 광고 삭제", description = "광고를 삭제합니다. S3 이미지도 함께 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "광고 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "광고 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/{advertisementId}")
    public ResponseEntity<CommonResponse<Void>> deleteAdvertisement(
            @Parameter(description = "광고 ID") @PathVariable Long advertisementId) {
        advertisementService.deleteAdvertisement(advertisementId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
