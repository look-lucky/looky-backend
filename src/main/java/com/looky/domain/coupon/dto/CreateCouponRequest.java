package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.CouponStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCouponRequest {

    @NotBlank(message = "쿠폰명은 필수입니다.")
    private String title;

    private String description;

    private Long targetOrganizationId;

    private LocalDateTime issueStartsAt;
    private LocalDateTime issueEndsAt;

    @NotNull(message = "총 발행 수량은 필수입니다.")
    private Integer totalQuantity;

    @NotNull(message = "인당 발행 한도는 필수입니다.")
    private Integer limitPerUser;

    private CouponStatus status; // 선택 입력 (로직에 따라 DRAFT 또는 ACTIVE로 기본 설정)

    private List<Long> targetItemIds; // 선택 입력 (상품 매핑)
}
