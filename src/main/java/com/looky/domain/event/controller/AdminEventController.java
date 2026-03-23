package com.looky.domain.event.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.event.dto.CreateEventRequest;
import com.looky.domain.event.dto.UpdateEventRequest;
import com.looky.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Event", description = "관리자 이벤트 관리 API")
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @Operation(summary = "[관리자] 이벤트 등록", description = "새로운 이벤트를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createEvent(
            @RequestBody @Valid CreateEventRequest request
    ) {
        Long eventId = eventService.createEventForAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(eventId));
    }

    @Operation(summary = "[관리자] 이벤트 수정", description = "이벤트 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "이벤트 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/{eventId}")
    public ResponseEntity<CommonResponse<Void>> updateEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventRequest request
    ) {
        eventService.updateEventForAdmin(eventId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 이벤트 삭제", description = "이벤트를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "이벤트 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "이벤트 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/{eventId}")
    public ResponseEntity<CommonResponse<Void>> deleteEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId) {
        eventService.deleteEventForAdmin(eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
