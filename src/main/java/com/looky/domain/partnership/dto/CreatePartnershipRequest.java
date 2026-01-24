package com.looky.domain.partnership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePartnershipRequest {

    @NotNull(message = "조직 ID는 필수입니다.")
    private Long organizationId;

    @NotBlank(message = "혜택 내용은 필수입니다.")
    private String benefit;
}
