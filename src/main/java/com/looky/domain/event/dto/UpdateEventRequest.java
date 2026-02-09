package com.looky.domain.event.dto;

import com.looky.domain.event.entity.EventStatus;
import com.looky.domain.event.entity.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "이벤트 수정 요청")
public class UpdateEventRequest {

    @Schema(description = "이벤트 제목", example = "2026 봄 플리마켓 (수정)")
    private JsonNullable<String> title = JsonNullable.undefined();

    @Schema(description = "이벤트 설명")
    private JsonNullable<String> description = JsonNullable.undefined();

    @Schema(description = "이벤트 타입 목록")
    private JsonNullable<List<EventType>> eventTypes = JsonNullable.undefined();

    @Schema(description = "위도")
    private JsonNullable<Double> latitude = JsonNullable.undefined();

    @Schema(description = "경도")
    private JsonNullable<Double> longitude = JsonNullable.undefined();

    @Schema(description = "이벤트 시작일시")
    private JsonNullable<LocalDateTime> startDateTime = JsonNullable.undefined();

    @Schema(description = "이벤트 종료일시")
    private JsonNullable<LocalDateTime> endDateTime = JsonNullable.undefined();

    @Schema(description = "이벤트 상태", example = "LIVE")
    private JsonNullable<EventStatus> status = JsonNullable.undefined();
}
