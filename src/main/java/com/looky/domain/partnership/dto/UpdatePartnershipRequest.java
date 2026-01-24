package com.looky.domain.partnership.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdatePartnershipRequest {

    @NotBlank(message = "혜택 내용은 필수입니다.")
    private String benefit;

    private LocalDate startsAt;

    private LocalDate endsAt;
}
