package com.looky.domain.event.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.event.dto.EventResponse;
import com.looky.domain.event.entity.EventStatus;
import com.looky.domain.event.entity.EventType;
import com.looky.domain.event.service.EventService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Event", description = "학생 이벤트 조회 API")
@RestController
@RequestMapping("/api/student/events")
@RequiredArgsConstructor
public class StudentEventController {

    private final EventService eventService;

    @Operation(summary = "[학생] 이벤트 단건 조회", description = "이벤트 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "이벤트 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/{eventId}")
    public ResponseEntity<CommonResponse<EventResponse>> getEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId) {
        EventResponse response = eventService.getEvent(eventId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 이벤트 목록 조회", description = "소속 대학 기준으로 이벤트 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<EventResponse>>> getEvents(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "검색 키워드 (제목)") @RequestParam(required = false) String keyword,
            @Parameter(description = "이벤트 타입 필터 (복수 선택 가능)") @RequestParam(required = false) List<EventType> eventTypes,
            @Parameter(description = "상태 필터") @RequestParam(required = false) EventStatus status,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 10) Pageable pageable) {
        Long userId = principalDetails.getUser().getId();
        PageResponse<EventResponse> response = eventService.getEventsForStudent(userId, keyword, eventTypes, status, pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
