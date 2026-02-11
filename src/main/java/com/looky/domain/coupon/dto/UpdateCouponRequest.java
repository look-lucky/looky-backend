package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.CouponBenefitType;
import com.looky.domain.coupon.entity.CouponStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "쿠폰 수정 요청")
public class UpdateCouponRequest {

    @Schema(description = "쿠폰명", example = "신규 가입 감사 쿠폰 (수정)")
    private JsonNullable<String> title = JsonNullable.undefined();


    @Schema(description = "발급 시작 일시")
    private JsonNullable<LocalDateTime> issueStartsAt = JsonNullable.undefined();

    @Schema(description = "발급 종료 일시")
    private JsonNullable<LocalDateTime> issueEndsAt = JsonNullable.undefined();

    @Schema(description = "총 발행 수량 (null일 경우 무제한)", example = "100")
    private JsonNullable<Integer> totalQuantity = JsonNullable.undefined();

    @Schema(description = "인당 발급 제한 수량", example = "1")
    private JsonNullable<Integer> limitPerUser = JsonNullable.undefined();

    @Schema(description = "혜택 타입 (DISCOUNT_AMOUNT, DISCOUNT_RATE 등)", example = "DISCOUNT_RATE")
    private JsonNullable<CouponBenefitType> benefitType = JsonNullable.undefined();

    @Schema(description = "혜택 값 (할인율 또는 할인금액)", example = "10")
    private JsonNullable<String> benefitValue = JsonNullable.undefined();

    @Schema(description = "최소 주문 금액", example = "10000")
    private JsonNullable<Integer> minOrderAmount = JsonNullable.undefined();

    @Schema(description = "쿠폰 상태 (ACTIVE, INACTIVE 등)", example = "ACTIVE")
    private JsonNullable<CouponStatus> status = JsonNullable.undefined();
}
