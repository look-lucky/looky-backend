package com.looky.domain.organization.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.openapitools.jackson.nullable.JsonNullable;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "대학교 정보 수정 요청")
public class UpdateUniversityRequest {

    @Schema(description = "대학교 이름", example = "한국대학교 (수정)")
    private JsonNullable<String> name = JsonNullable.undefined();

    @Schema(description = "학교 이메일 도메인", example = "korea.ac.kr")
    private JsonNullable<String> emailDomain = JsonNullable.undefined();
}
