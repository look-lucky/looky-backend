package com.looky.domain.review.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.review.dto.*;
import com.looky.domain.review.service.ReviewService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Owner Review", description = "점주 리뷰 답글 관리 API")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "[점주] 리뷰 답글 작성", description = "본인 상점의 리뷰에 답글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "답글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 또는 원본 리뷰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/stores/{storeId}/reviews/{parentReviewId}/replies")
    public ResponseEntity<CommonResponse<Long>> createReply(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(description = "원본 리뷰 ID") @PathVariable Long parentReviewId,
            @RequestBody @Valid CreateReviewRequest request
    ) {
        Long reviewId = reviewService.createReplyForOwner(principalDetails.getUser(), storeId, parentReviewId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(reviewId));
    }

    @Operation(summary = "[점주] 답글 수정", description = "작성한 답글을 수정합니다. (본인만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답글 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 답글 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "답글 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<CommonResponse<Void>> updateReview(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @RequestBody @Valid UpdateReviewRequest request
    ) {
        reviewService.updateReviewForOwner(reviewId, principalDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 답글 삭제", description = "작성한 답글을 삭제합니다. (본인만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "답글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 답글 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "답글 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<CommonResponse<Void>> deleteReview(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId
    ) {
        reviewService.deleteReviewForOwner(reviewId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 상점 리뷰 목록 조회", description = "특정 상점의 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<CommonResponse<PageResponse<OwnerReviewResponse>>> getReviews(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<OwnerReviewResponse> reviews = reviewService.getReviewsForOwner(storeId, pageable);
        return ResponseEntity.ok(CommonResponse.success(PageResponse.from(reviews)));
    }

    @Operation(summary = "[점주] 내 답글 목록 조회", description = "점주가 작성한 답글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/reviews/my")
    public ResponseEntity<CommonResponse<PageResponse<OwnerReviewResponse>>> getMyReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<OwnerReviewResponse> reviews = reviewService.getMyReviewsForOwner(principalDetails.getUser(), pageable);
        return ResponseEntity.ok(CommonResponse.success(PageResponse.from(reviews)));
    }

    @Operation(summary = "[점주] 상점 리뷰 통계 조회", description = "상점의 평점 평균, 총 리뷰 수, 별점별 개수 분포를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}/reviews/stats")
    public ResponseEntity<CommonResponse<ReviewStatsResponse>> getReviewStats(
            @Parameter(description = "상점 ID") @PathVariable Long storeId
    ) {
        ReviewStatsResponse response = reviewService.getReviewStatsForOwner(storeId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 리뷰 신고", description = "특정 리뷰를 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 신고 성공"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/reviews/{reviewId}/reports")
    public ResponseEntity<CommonResponse<Void>> reportReview(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @RequestBody @Valid ReportRequest request
    ) {
        reviewService.reportReviewForOwner(reviewId, principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 리뷰 좋아요", description = "리뷰에 좋아요를 누릅니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 성공"),
            @ApiResponse(responseCode = "400", description = "자신의 리뷰에 좋아요 시도", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 좋아요 누름", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/reviews/{reviewId}/likes")
    public ResponseEntity<CommonResponse<Void>> addLike(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId
    ) {
        reviewService.addLikeForOwner(principalDetails.getUser(), reviewId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 리뷰 좋아요 취소", description = "리뷰 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
            @ApiResponse(responseCode = "404", description = "좋아요를 누르지 않은 리뷰", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/reviews/{reviewId}/likes")
    public ResponseEntity<CommonResponse<Void>> removeLike(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId
    ) {
        reviewService.removeLikeForOwner(principalDetails.getUser(), reviewId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
