package com.looky.domain.advertisement.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.advertisement.dto.AdvertisementResponse;
import com.looky.domain.advertisement.service.AdvertisementService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Advertisement", description = "광고 조회 API")
@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "[공통] 팝업 광고 목록 조회", description = "현재 노출 중인 팝업 광고 목록을 노출 순서대로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/popup")
    public ResponseEntity<CommonResponse<List<AdvertisementResponse>>> getPopupAdvertisements(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<AdvertisementResponse> response = advertisementService.getActivePopupAdvertisements(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[공통] 배너 광고 목록 조회", description = "현재 노출 중인 배너 광고 목록을 노출 순서대로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/banner")
    public ResponseEntity<CommonResponse<List<AdvertisementResponse>>> getBannerAdvertisements(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<AdvertisementResponse> response = advertisementService.getActiveBannerAdvertisements(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[공통] 플로팅 배너 목록 조회", description = "현재 노출 중인 플로팅 배너 목록을 노출 순서대로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/floating")
    public ResponseEntity<CommonResponse<List<AdvertisementResponse>>> getFloatingAdvertisements(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<AdvertisementResponse> response = advertisementService.getActiveFloatingAdvertisements(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
