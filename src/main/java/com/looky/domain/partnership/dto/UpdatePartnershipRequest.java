package com.looky.domain.partnership.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@Schema(description = "제휴 정보 수정 요청")
public class UpdatePartnershipRequest {

    @Schema(description = "제휴 혜택 내용", example = "학생증 제시 시 10% 할인 (수정)")
    private JsonNullable<String> benefit = JsonNullable.undefined();

    @Schema(description = "제휴 시작일")
    private JsonNullable<LocalDate> startsAt = JsonNullable.undefined();

    @Schema(description = "제휴 종료일")
    private JsonNullable<LocalDate> endsAt = JsonNullable.undefined();
}
