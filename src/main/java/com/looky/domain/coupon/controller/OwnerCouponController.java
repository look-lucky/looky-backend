package com.looky.domain.coupon.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.coupon.dto.CreateCouponRequest;
import com.looky.domain.coupon.dto.OwnerCouponResponse;
import com.looky.domain.coupon.dto.UpdateCouponRequest;
import com.looky.domain.coupon.dto.VerifyCouponRequest;
import com.looky.domain.coupon.dto.VerifyCouponResponse;
import com.looky.domain.coupon.service.CouponService;
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

@Tag(name = "Owner Coupon", description = "점주 쿠폰 관리 API")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerCouponController {

    private final CouponService couponService;

    @Operation(summary = "[점주] 쿠폰 생성", description = "상점에 새로운 쿠폰을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쿠폰 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/stores/{storeId}/coupons")
    public ResponseEntity<CommonResponse<Long>> createCoupon(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid CreateCouponRequest request
    ) {
        Long couponId = couponService.createCoupon(storeId, principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(couponId));
    }

    @Operation(summary = "[점주] 쿠폰 수정", description = "쿠폰 정보를 수정합니다. (본인 상점만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/coupons/{couponId}")
    public ResponseEntity<CommonResponse<Void>> updateCoupon(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UpdateCouponRequest request
    ) {
        couponService.updateCoupon(couponId, principalDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 쿠폰 삭제", description = "쿠폰을 삭제합니다. (본인 상점만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "쿠폰 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/coupons/{couponId}")
    public ResponseEntity<CommonResponse<Void>> deleteCoupon(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        couponService.deleteCoupon(couponId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 쿠폰 수동 만료", description = "점주가 자신의 쿠폰을 수동으로 만료 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 만료 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/coupons/{couponId}/expire")
    public ResponseEntity<CommonResponse<Void>> expireCoupon(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        couponService.expireCoupon(couponId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 쿠폰 코드 조회 (검증)", description = "손님이 제시한 4자리 코드를 입력하여 쿠폰 및 사용자 정보를 확인합니다. (상태 변경 없음)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 코드이거나 활성화되지 않은 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "만료된 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/stores/{storeId}/coupons/verify")
    public ResponseEntity<CommonResponse<VerifyCouponResponse>> verifyCoupon(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid VerifyCouponRequest request
    ) {
        VerifyCouponResponse response = couponService.verifyCouponCode(storeId, principalDetails.getUser(), request.getCode());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 쿠폰 사용 확정", description = "조회된 쿠폰을 실제로 사용 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 사용 완료"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "활성화되지 않은 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/stores/{storeId}/coupons/{studentCouponId}/use")
    public ResponseEntity<CommonResponse<Void>> useCoupon(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(description = "학생 쿠폰 ID") @PathVariable Long studentCouponId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        couponService.useCoupon(storeId, principalDetails.getUser(), studentCouponId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 상점별 쿠폰 목록 조회", description = "본인 상점의 모든 쿠폰을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}/coupons")
    public ResponseEntity<CommonResponse<List<OwnerCouponResponse>>> getCouponsByStore(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<OwnerCouponResponse> response = couponService.getCouponsByStoreForOwner(storeId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
