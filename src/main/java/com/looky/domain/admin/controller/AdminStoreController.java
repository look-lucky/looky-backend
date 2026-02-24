package com.looky.domain.admin.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.admin.service.AdminStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.looky.common.service.GeocodingService;
import com.looky.common.service.GeocodingService.Coordinate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStoreController {

    private final AdminStoreService adminStoreService;
    private final GeocodingService geocodingService;

    @Operation(summary = "[관리자] 상점 데이터 엑셀 업로드", description = "상권 데이터를 엑셀로 업로드하여 DB에 저장 및 보정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공 (작업 시작됨)"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping(value = "/stores/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<String>> uploadStoreData(
            @Parameter(description = "엑셀 파일 (.xlsx)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("file") MultipartFile file) {
        adminStoreService.uploadStoreData(file);
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
