package com.looky.domain.organization.dto;

import com.looky.domain.organization.entity.OrganizationCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.openapitools.jackson.nullable.JsonNullable;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "조직(단과대/학과 등) 수정 요청")
public class UpdateOrganizationRequest {

    @Schema(description = "조직 카테고리 (COLLEGE, DEPARTMENT 등)", example = "DEPARTMENT")
    private JsonNullable<OrganizationCategory> category = JsonNullable.undefined();

    @Schema(description = "조직 이름", example = "컴퓨터공학과 (수정)")
    private JsonNullable<String> name = JsonNullable.undefined();

    @Schema(description = "상위 조직 ID (예: 단과대 ID)", example = "10")
    private JsonNullable<Long> parentId = JsonNullable.undefined();

    @Schema(description = "만료 일시")
    private JsonNullable<LocalDateTime> expiresAt = JsonNullable.undefined();
}
