package com.looky.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "리뷰 수정 요청")
public class UpdateReviewRequest {

    @Schema(description = "리뷰 내용", example = "맛있어요! 사장님이 친절해요.")
    private JsonNullable<String> content = JsonNullable.undefined();

    @Schema(description = "별점 (1~5)", example = "5")
    private JsonNullable<Integer> rating = JsonNullable.undefined();

    @Schema(description = "유지할 이미지 ID 목록 (누락된 ID는 삭제됨)")
    private JsonNullable<List<Long>> preserveImageIds = JsonNullable.undefined();
}
