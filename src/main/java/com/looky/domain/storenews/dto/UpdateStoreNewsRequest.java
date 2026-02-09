package com.looky.domain.storenews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@NoArgsConstructor
@Schema(description = "가게 소식 수정 요청")
public class UpdateStoreNewsRequest {

    @Schema(description = "소식 제목", example = "오늘의 할인 이벤트! (수정)")
    private JsonNullable<String> title = JsonNullable.undefined();

    @Schema(description = "소식 내용", example = "오늘 하루만 전 메뉴 20% 할인합니다.")
    private JsonNullable<String> content = JsonNullable.undefined();

}
