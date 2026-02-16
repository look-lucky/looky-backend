package com.looky.domain.inquiry.controller;

import com.looky.security.details.PrincipalDetails;
import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.inquiry.dto.CreateInquiryRequest;
import com.looky.domain.inquiry.dto.InquiryResponse;
import com.looky.domain.inquiry.service.InquiryService;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "고객센터 API")
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(summary = "문의하기", description = "고객센터에 문의를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 (제목 길이, 이미지 개수 초과 등)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Long>> createInquiry(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException, MethodArgumentNotValidException {
        CreateInquiryRequest request = objectMapper.readValue(requestJson, CreateInquiryRequest.class);
        validateRequest(request);

        Long inquiryId = inquiryService.createInquiry(principalDetails.getUser(), request, images);
        return ResponseEntity.ok(CommonResponse.success(inquiryId));
    }

    @Operation(summary = "내 문의 목록 조회", description = "내가 작성한 문의 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<InquiryResponse>>> getInquiries(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "페이징 정보") @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<InquiryResponse> response = inquiryService.getInquiries(principalDetails.getUser(), pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "문의 상세 조회", description = "문의 상세 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "본인 문의 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문의 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/{inquiryId}")
    public ResponseEntity<CommonResponse<InquiryResponse>> getInquiry(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long inquiryId
    ) {
        InquiryResponse response = inquiryService.getInquiry(principalDetails.getUser(), inquiryId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    private <T> void validateRequest(T request) throws MethodArgumentNotValidException {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, request.getClass().getName());
            for (ConstraintViolation<T> violation : violations) {
                bindingResult.addError(new ObjectError(request.getClass().getName(), violation.getMessage()));
            }
            throw new MethodArgumentNotValidException(null, bindingResult);
        }
    }
}
