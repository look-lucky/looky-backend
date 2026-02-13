package com.looky.domain.event.dto;

import com.looky.domain.event.entity.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "이벤트 생성 요청")
public class CreateEventRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "이벤트 제목", example = "2024 봄 플리마켓")
    private String title;

    @Schema(description = "이벤트 설명", example = "다양한 수공예품과 먹거리를 만나보세요!")
    private String description;

    @Schema(description = "이벤트 부제목", example = "봄맞이 대축제")
    private String subtitle;

    @NotEmpty(message = "이벤트 타입은 최소 1개 이상 선택해야 합니다.")
    @Schema(description = "이벤트 타입 목록", example = "[\"FLEA_MARKET\", \"FOOD_EVENT\"]")
    private List<EventType> eventTypes;

    @Schema(description = "위도", example = "37.5665")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    private Double longitude;

    @NotNull(message = "시작일시는 필수입니다.")
    @Schema(description = "이벤트 시작일시", example = "2024-03-01T10:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "이벤트 종료일시", example = "2024-03-01T18:00:00")
    private LocalDateTime endDateTime;

    @NotBlank(message = "장소는 필수입니다.")
    @Schema(description = "장소", example = "학생회관 1층")
    private String place;

    @NotNull(message = "대학교 ID는 필수입니다.")
    @Schema(description = "대학교 ID", example = "1")
    private Long universityId; // 0은 모든 학교 대상
}
