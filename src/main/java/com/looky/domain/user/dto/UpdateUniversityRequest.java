package com.looky.domain.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateUniversityRequest {
    @NotNull(message = "대학 ID는 필수입니다.")
    private Long universityId;
}
