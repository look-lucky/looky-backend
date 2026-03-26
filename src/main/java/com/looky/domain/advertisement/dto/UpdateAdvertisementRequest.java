package com.looky.domain.advertisement.dto;

import com.looky.domain.advertisement.entity.AdvertisementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "광고 수정 요청")
public class UpdateAdvertisementRequest {

    @Schema(description = "광고 제목", example = "여름 이벤트 팝업 광고 (수정)")
    private JsonNullable<String> title = JsonNullable.undefined();

    @Schema(description = "광고 이미지 URL")
    private JsonNullable<String> imageUrl = JsonNullable.undefined();

    @Schema(description = "랜딩 URL (null 전송 시 삭제)", nullable = true)
    private JsonNullable<String> landingUrl = JsonNullable.undefined();

    @Schema(description = "노출 순서 (낮을수록 우선 노출)", example = "1")
    private JsonNullable<Integer> displayOrder = JsonNullable.undefined();

    @Schema(description = "노출 시작일시", example = "2026-04-01T00:00:00")
    private JsonNullable<LocalDateTime> startAt = JsonNullable.undefined();

    @Schema(description = "노출 종료일시", example = "2026-04-30T23:59:59")
    private JsonNullable<LocalDateTime> endAt = JsonNullable.undefined();

    @Schema(description = "광고 상태 (ACTIVE / INACTIVE만 직접 변경 가능)", example = "INACTIVE")
    private JsonNullable<AdvertisementStatus> status = JsonNullable.undefined();
}
