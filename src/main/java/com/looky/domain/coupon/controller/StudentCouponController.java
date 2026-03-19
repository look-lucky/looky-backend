package com.looky.domain.coupon.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.coupon.dto.ActivateCouponResponse;
import com.looky.domain.coupon.dto.DownloadCouponResponse;
import com.looky.domain.coupon.dto.StudentCouponResponse;
import com.looky.domain.coupon.service.CouponService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Coupon", description = "학생 쿠폰 API")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentCouponController {

    private final CouponService couponService;

    @Operation(summary = "[학생] 상점별 쿠폰 목록 조회", description = "특정 상점의 학생용 쿠폰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "학생 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}/coupons")
    public ResponseEntity<CommonResponse<List<StudentCouponResponse>>> getCouponsByStore(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<StudentCouponResponse> response = couponService.getCouponsByStoreForStudent(storeId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 오늘의 신규 쿠폰 조회", description = "학생의 소속 대학과 제휴된 매장에서 24시간 내에 발급된 쿠폰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "학생 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "학생 프로필 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/coupons/today")
    public ResponseEntity<CommonResponse<List<StudentCouponResponse>>> getTodayCoupons(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<StudentCouponResponse> response = couponService.getTodayCoupons(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 쿠폰 다운로드", description = "학생이 쿠폰을 다운로드받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 다운로드 성공"),
            @ApiResponse(responseCode = "404", description = "쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "재고 소진 / 발급 기간 아님 / 한도 초과", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/coupons/{couponId}/download")
    public ResponseEntity<CommonResponse<DownloadCouponResponse>> downloadCoupon(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        DownloadCouponResponse response = couponService.downloadCoupon(couponId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 쿠폰 코드 발급", description = "매장에서 사용하기 위해 쿠폰을 활성화하고 4자리 코드를 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 활성화 성공 (코드 반환)"),
            @ApiResponse(responseCode = "409", description = "이미 사용한 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "유효기간 만료", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "발급한 쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/my-coupons/{studentCouponId}/activate")
    public ResponseEntity<CommonResponse<ActivateCouponResponse>> activateCoupon(
            @Parameter(description = "사용할 쿠폰 ID (download ID)") @PathVariable Long studentCouponId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        ActivateCouponResponse response = couponService.activateCoupon(studentCouponId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 내 쿠폰 조회", description = "학생이 발급받은 쿠폰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "학생 권한 필요", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/my-coupons")
    public ResponseEntity<CommonResponse<List<DownloadCouponResponse>>> getMyCoupons(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<DownloadCouponResponse> response = couponService.getMyCoupons(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
