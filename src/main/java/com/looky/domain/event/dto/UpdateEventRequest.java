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

    @Schema(description = "이벤트 부제목")
    private JsonNullable<String> subtitle = JsonNullable.undefined();

    @Schema(description = "이벤트 타입 목록")
    private JsonNullable<List<EventType>> eventTypes = JsonNullable.undefined();

    @Schema(description = "장소")
    private JsonNullable<String> place = JsonNullable.undefined();

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

    @Schema(description = "대학교 ID (null이면 모든 대학)", example = "null", nullable = true)
    private JsonNullable<Long> universityId = JsonNullable.undefined();

    @Schema(description = "유지할 일반 이미지 ID 목록 (해당 배열에 없는 요소는 배열에서 제외됩니다. 명시적으로 null을 보내면 모든 갤러리 이미지가 삭제됩니다. 전달하지 않으면 이미지를 수정하지 않습니다.)")
    private JsonNullable<List<Long>> preserveImageIds = JsonNullable.undefined();
}
