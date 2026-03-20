package com.looky.domain.store.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.common.service.GeocodingService;
import com.looky.common.service.GeocodingService.Coordinate;
import com.looky.domain.store.dto.StoreCreateRequest;
import com.looky.domain.store.dto.StoreUpdateRequest;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin Store", description = "관리자 상점 관리 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStoreController {

    private final GeocodingService geocodingService;
    private final StoreService storeService;

    @Operation(summary = "[관리자] 상점 등록", description = "관리자가 새로운 상점을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상점 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 상점 (상점명 + 지점명 기준)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/stores")
    public ResponseEntity<CommonResponse<Long>> createStore(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid StoreCreateRequest request
    ) {
        Long storeId = storeService.createStoreForAdmin(principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(storeId));
    }

    @Operation(summary = "[관리자] 상점 정보 수정", description = "UNCLAIMED 상태의 상점 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상점 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 UNCLAIMED 상태 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 상점 (상점명 + 지점명 기준)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/stores/{storeId}")
    public ResponseEntity<CommonResponse<Void>> updateStore(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @RequestBody @Valid StoreUpdateRequest request
    ) {
        storeService.updateStoreForAdmin(storeId, principalDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 상점 삭제", description = "UNCLAIMED 상태의 상점을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "상점 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 UNCLAIMED 상태 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<CommonResponse<Void>> deleteStore(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        storeService.deleteStoreForAdmin(storeId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 상점 데이터 엑셀 업로드", description = "상권 데이터를 엑셀로 업로드하여 DB에 저장 및 보정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공 (작업 시작됨)"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping(value = "/stores/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<String>> uploadStoreData(
            @Parameter(description = "엑셀 파일 (.xlsx)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("file") MultipartFile file) {
        storeService.uploadStoreDataForAdmin(file);
        return ResponseEntity.ok(CommonResponse.success("상점 데이터 업로드가 정상적으로 시작되었습니다.)"));
    }

    @Operation(summary = "[관리자] 주소로 위경도 변환", description = "주소를 입력받아 위도, 경도 좌표를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변환 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주소/API 호출 에러", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/geocode")
    public ResponseEntity<CommonResponse<Coordinate>> getGeocode(
            @Parameter(description = "도로명 주소 (예: 전라북도 전주시 덕진구 명륜3길 22)") @RequestParam String address) {
        Coordinate coordinate = geocodingService.getCoordinate(address);
        return ResponseEntity.ok(CommonResponse.success(coordinate));
    }
}
