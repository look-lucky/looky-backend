package com.looky.domain.admin.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.inquiry.dto.InquiryResponse;
import com.looky.domain.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Inquiry", description = "관리자 문의 관리 API")
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "[관리자] 문의 전체 목록 조회", description = "모든 사용자의 문의 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<InquiryResponse>>> getAllInquiries(
            @Parameter(description = "페이징 정보") @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<InquiryResponse> page = inquiryService.getAllInquiries(pageable);
        return ResponseEntity.ok(CommonResponse.success(PageResponse.from(page)));
    }
}
