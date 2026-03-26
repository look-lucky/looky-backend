package com.looky.domain.coupon.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.coupon.dto.*;
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

@Tag(name = "Coupon", description = "쿠폰 관련 API")
@Deprecated
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
                Long couponId = couponService.createCouponForOwner(storeId, principalDetails.getUser(), request);
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
                couponService.updateCouponForOwner(couponId, principalDetails.getUser(), request);
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
                couponService.deleteCouponForOwner(couponId, principalDetails.getUser());
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
        }

        @Operation(summary = "[점주] 쿠폰 수동 만료", description = "점주가 자신의 쿠폰을 수동으로 만료시킵니다.")
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
            couponService.expireCouponForOwner(couponId, principalDetails.getUser());
            return ResponseEntity.ok(CommonResponse.success(null));
        }

        @Operation(summary = "[점주] 쿠폰 코드 조회 (검증)", description = "손님이 제시한 4자리 코드를 입력하여 혜택 및 사용자 정보를 확인합니다. (상태 변경 없음)")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "쿠폰 조회 성공"),
                @ApiResponse(responseCode = "404", description = "유효하지 않은 코드 또는 활성화되지 않은 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "422", description = "만료된 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/stores/{storeId}/coupons/verify")
        public ResponseEntity<CommonResponse<VerifyCouponResponse>> verifyCoupon(
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @RequestBody @Valid VerifyCouponRequest request
        )
        {
                VerifyCouponResponse response = couponService.verifyCouponCodeForOwner(storeId, principalDetails.getUser(), request.getCode());
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[점주] 쿠폰 사용 확정", description = "조회된 쿠폰을 실제로 사용 처리합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "쿠폰 사용 완료"),
                @ApiResponse(responseCode = "404", description = "쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "422", description = "활성화되지 않은 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/stores/{storeId}/coupons/{studentCouponId}/use")
        public ResponseEntity<CommonResponse<Void>> useCoupon(
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @Parameter(description = "학생 쿠폰 ID") @PathVariable Long studentCouponId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                couponService.useCouponForOwner(storeId, principalDetails.getUser(), studentCouponId);
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
                @Parameter(description = "상점 ID") @PathVariable Long storeId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                List<CouponResponse> response = couponService.getCouponsByStore(storeId, principalDetails != null ? principalDetails.getUser() : null);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        // --- 학생용 ---

        @Operation(summary = "[학생] 오늘의 신규 쿠폰 조회", description = "학생의 학교와 제휴된 매장에서 24시간 이내에 발급된 쿠폰 목록을 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "성공"),
                @ApiResponse(responseCode = "403", description = "학생 권한 필요")
        })
        @GetMapping("/coupons/today")
        public ResponseEntity<CommonResponse<List<StudentCouponResponse>>> getTodayCoupons(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        ) {
                List<StudentCouponResponse> response = couponService.getTodayCouponsForStudent(principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[학생] 쿠폰 다운로드", description = "사용자가 쿠폰을 다운로드받습니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "쿠폰 다운로드 성공"),
                        @ApiResponse(responseCode = "404", description = "쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "재고 소진 / 발급 기간 아님 / 한도 초과", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/coupons/{couponId}/download")
        public ResponseEntity<CommonResponse<DownloadCouponResponse>> downloadCoupon(
                @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                DownloadCouponResponse response = couponService.downloadCouponForStudent(couponId, principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[학생] 쿠폰 코드 발급", description = "매장에서 사용하기 위해 쿠폰을 활성화하고 4자리 코드를 발급받습니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "쿠폰 활성화 성공 (코드 반환)"),
                        @ApiResponse(responseCode = "409", description = "이미 사용된 쿠폰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "유효기간 만료", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "발급된 쿠폰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/my-coupons/{studentCouponId}/activate")
        public ResponseEntity<CommonResponse<ActivateCouponResponse>> activateCoupon(
                @Parameter(description = "사용자 쿠폰 ID (download ID)") @PathVariable Long studentCouponId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                ActivateCouponResponse response = couponService.activateCouponForStudent(studentCouponId, principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[학생] 내 쿠폰 조회", description = "사용자가 발급받은 쿠폰 목록을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공")
        })
        @GetMapping("/my-coupons")
        public ResponseEntity<CommonResponse<List<DownloadCouponResponse>>> getMyCoupons(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                List<DownloadCouponResponse> response = couponService.getMyCouponsForStudent(principalDetails.getUser());
                return ResponseEntity.ok(CommonResponse.success(response));
        }
}
