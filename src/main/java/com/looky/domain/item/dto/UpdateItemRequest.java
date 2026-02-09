package com.looky.domain.item.dto;

import com.looky.domain.item.entity.ItemBadge;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@NoArgsConstructor
@Schema(description = "메뉴(상품) 수정 요청")
public class UpdateItemRequest {

    @Schema(description = "메뉴명", example = "아이스 아메리카노 (수정)")
    private JsonNullable<String> name = JsonNullable.undefined();

    @Schema(description = "가격", example = "4500")
    private JsonNullable<Integer> price = JsonNullable.undefined();

    @Schema(description = "메뉴 설명", example = "고소한 원두를 사용한 아메리카노입니다.")
    private JsonNullable<String> description = JsonNullable.undefined();

    @Schema(description = "품절 여부", example = "true")
    private JsonNullable<Boolean> isSoldOut = JsonNullable.undefined();

    @Schema(description = "메뉴 정렬 순서", example = "1")
    private JsonNullable<Integer> itemOrder = JsonNullable.undefined();

    @Schema(description = "대표 메뉴 여부", example = "true")
    private JsonNullable<Boolean> isRepresentative = JsonNullable.undefined();

    @Schema(description = "숨김 여부 (키오스크 등에서 미노출)", example = "false")
    private JsonNullable<Boolean> isHidden = JsonNullable.undefined();

    @Schema(description = "뱃지 (NEW, BEST, HOT 등)", example = "BEST")
    private JsonNullable<ItemBadge> badge = JsonNullable.undefined();

    @Schema(description = "카테고리 ID (null 전달 시 카테고리 해제)", example = "1")
    private JsonNullable<Long> itemCategoryId = JsonNullable.undefined();
}