package com.looky.domain.partnership.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePartnershipRequest {

    @NotBlank(message = "혜택 내용은 필수입니다.")
    private String benefit;
}
