package com.looky.domain.admin.controller;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Set;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Admin Event", description = "관리자 이벤트 관리 API")
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(summary = "[관리자] 이벤트 등록", description = "새로운 이벤트를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Long>> createEvent(
            @RequestPart("request") String requestJson,
            @Parameter(description = "이벤트 이미지") @RequestPart(required = false) List<MultipartFile> images
    ) throws IOException, MethodArgumentNotValidException {

        CreateEventRequest request = objectMapper.readValue(requestJson, CreateEventRequest.class);
        validateRequest(request);

        Long eventId = eventService.createEvent(request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(eventId));
    }

    @Operation(summary = "[관리자] 이벤트 수정", description = "이벤트 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "이벤트 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping(value = "/{eventId}", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Void>> updateEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @RequestPart("request") String requestJson,
            @Parameter(description = "이벤트 이미지") @RequestPart(required = false) List<MultipartFile> images
    ) throws IOException, MethodArgumentNotValidException {
        UpdateEventRequest request = objectMapper.readValue(requestJson, UpdateEventRequest.class);
        validateRequest(request);

        eventService.updateEvent(eventId, request, images);
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
        eventService.deleteEvent(eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    private <T> void validateRequest(T request) throws MethodArgumentNotValidException {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, request.getClass().getName());
            for (ConstraintViolation<T> violation : violations) {
                bindingResult.addError(new FieldError(
                        request.getClass().getName(),
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        false,
                        null,
                        null,
                        violation.getMessage()
                ));
            }
            try {
                MethodParameter parameter = new MethodParameter(
                        this.getClass().getDeclaredMethod("validateRequest", Object.class), 0);
                throw new MethodArgumentNotValidException(parameter, bindingResult);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
