package com.looky.domain.user.dto;

import com.looky.domain.user.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "점주 정보 응답")
public class OwnerInfoResponse {
    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "owner@example.com")
    private String email;

    @Schema(description = "아이디", example = "owner123")
    private String username;

    @Schema(description = "성별", example = "MALE")
    private Gender gender;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthDate;
}
