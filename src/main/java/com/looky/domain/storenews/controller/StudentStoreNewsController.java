package com.looky.domain.storenews.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.storenews.dto.*;
import com.looky.domain.storenews.service.StoreNewsService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Student StoreNews", description = "학생 가게 소식 API")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentStoreNewsController {

    private final StoreNewsService storeNewsService;

    @Operation(summary = "[학생] 소식 목록 조회", description = "가게의 소식 목록을 조회합니다.")
    @GetMapping("/stores/{storeId}/news")
    public ResponseEntity<CommonResponse<PageResponse<StoreNewsResponse>>> getStoreNewsList(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "가게 ID") @PathVariable Long storeId,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<StoreNewsResponse> response = storeNewsService.getStoreNewsListForStudent(
                storeId, pageable, principalDetails != null ? principalDetails.getUser() : null);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 소식 상세 조회", description = "소식 상세 정보를 조회합니다.")
    @GetMapping("/store-news/{newsId}")
    public ResponseEntity<CommonResponse<StoreNewsResponse>> getStoreNews(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "소식 ID") @PathVariable Long newsId) {
        StoreNewsResponse response = storeNewsService.getStoreNewsForStudent(
                newsId, principalDetails != null ? principalDetails.getUser() : null);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 소식 좋아요 토글", description = "소식에 좋아요를 누르거나 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "소식 찾을 수 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/store-news/{newsId}/likes")
    public ResponseEntity<CommonResponse<Void>> toggleLike(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "소식 ID") @PathVariable Long newsId) {
        storeNewsService.toggleLikeForStudent(newsId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[학생] 댓글 작성", description = "소식에 댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "404", description = "소식 찾을 수 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/store-news/{newsId}/comments")
    public ResponseEntity<CommonResponse<Long>> createComment(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @RequestBody @Valid CreateStoreNewsCommentRequest request) {
        Long commentId = storeNewsService.createCommentForStudent(newsId, principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(commentId));
    }

    @Operation(summary = "[학생] 댓글 목록 조회", description = "소식의 댓글 목록을 조회합니다.")
    @GetMapping("/store-news/{newsId}/comments")
    public ResponseEntity<CommonResponse<PageResponse<StoreNewsCommentResponse>>> getComments(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<StoreNewsCommentResponse> response = storeNewsService.getCommentsForStudent(
                newsId, pageable, principalDetails != null ? principalDetails.getUser() : null);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 댓글 삭제", description = "자신의 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 댓글 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글 찾을 수 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/store-news/{newsId}/comments/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {
        storeNewsService.deleteCommentForStudent(commentId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
