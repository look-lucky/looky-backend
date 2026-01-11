package com.neardeal.domain.coupon.controller;

import com.neardeal.common.response.CommonResponse;
import com.neardeal.common.response.SwaggerErrorResponse;
import com.neardeal.domain.coupon.dto.*;
import com.neardeal.domain.coupon.service.CouponService;
import com.neardeal.security.details.PrincipalDetails;
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

@Tag(name = "Coupon", description = "쿠폰 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponController {

        private final CouponService couponService;

        // --- 점주용 ---
        @Operation(summary = "[점주] 쿠폰 생성", description = "상점의 새로운 쿠폰을 생성합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "쿠폰 생성 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (타 상점 물품 등)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/stores/{storeId}/coupons")
        public ResponseEntity<CommonResponse<Long>> createCoupon(
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @RequestBody @Valid CreateCouponRequest request
        )
        {
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
        )
        {
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
        )
        {
                couponService.deleteCoupon(couponId, principalDetails.getUser());
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
        }

        @Operation(summary = "[점주] 쿠폰 사용 확인 (코드 검증)", description = "손님이 제시한 4자리 코드를 입력하여 사용 처리합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "쿠폰 사용 완료"),
                @ApiResponse(responseCode = "404", description = "유효하지 않은 코드 또는 활성화되지 않은 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/stores/{storeId}/coupons/verify")
        public ResponseEntity<CommonResponse<Void>> verifyCoupon(
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @RequestBody @Valid VerifyCouponRequest request
        )
        {

                couponService.verifyAndUseCoupon(storeId, principalDetails.getUser(), request.getCode());
                return ResponseEntity.ok(CommonResponse.success(null));
        }


        // --- 공통 ---
        @Operation(summary = "[공통] 상점별 쿠폰 목록 조회", description = "특정 상점의 모든 쿠폰을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/stores/{storeId}/coupons")
        public ResponseEntity<CommonResponse<List<CouponResponse>>> getCouponsByStore(
                @Parameter(description = "상점 ID") @PathVariable Long storeId
        )
        {
                List<CouponResponse> response = couponService.getCouponsByStore(storeId);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[공통] 상품별 적용 가능 쿠폰 조회", description = "특정 상품에 적용 가능한 쿠폰 목록을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/items/{itemId}/coupons")
        public ResponseEntity<CommonResponse<List<CouponResponse>>> getCouponsByItem(
                @Parameter(description = "상품 ID") @PathVariable Long itemId
        )
        {
                List<CouponResponse> response = couponService.getCouponsByItem(itemId);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        // --- 학생용 ---
        @Operation(summary = "[학생] 쿠폰 발급", description = "사용자가 쿠폰을 발급받습니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "쿠폰 발급 성공"),
                        @ApiResponse(responseCode = "404", description = "쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "재고 소진 / 발급 기간 아님 / 한도 초과", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/coupons/{couponId}/issue")
        public ResponseEntity<CommonResponse<IssueCouponResponse>> issueCoupon(
                @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                IssueCouponResponse response = couponService.issueCoupon(couponId, principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[학생] 쿠폰 코드 발급", description = "매장에서 사용하기 위해 쿠폰을 활성화하고 4자리 코드를 발급받습니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "쿠폰 활성화 성공 (코드 반환)"),
                        @ApiResponse(responseCode = "409", description = "이미 사용된 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "유효기간 만료", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "발급된 쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/my-coupons/{customerCouponId}/activate")
        public ResponseEntity<CommonResponse<String>> activateCoupon(
                @Parameter(description = "사용자 쿠폰 ID (issue ID)") @PathVariable Long customerCouponId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                String verificationCode = couponService.activateCoupon(customerCouponId, principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(verificationCode));
        }

        @Operation(summary = "[학생] 내 쿠폰 조회", description = "사용자가 발급받은 쿠폰 목록을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공")
        })
        @GetMapping("/my-coupons")
        public ResponseEntity<CommonResponse<List<IssueCouponResponse>>> getMyCoupons(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                List<IssueCouponResponse> response = couponService.getMyCoupons(principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }
}
