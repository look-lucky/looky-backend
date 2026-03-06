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

    @Schema(description = "이미지 URL 목록 (미전송 시 유지, null/빈배열 전송 시 전체 삭제, 배열 전송 시 해당 목록으로 교체)")
    private JsonNullable<List<String>> imageUrls = JsonNullable.undefined();
}
