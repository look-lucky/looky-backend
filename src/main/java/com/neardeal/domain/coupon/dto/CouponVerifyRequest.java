package com.neardeal.domain.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CouponVerifyRequest {

    @NotBlank(message = "코드는 필수입니다.")
    @Size(min = 4, max = 4, message = "코드는 4자리여야 합니다.")
    private String code;
}
