package com.looky.domain.storenews.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
import com.looky.domain.storenews.dto.*;
import com.looky.domain.storenews.service.StoreNewsService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Tag(name = "StoreNews", description = "가게 소식 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreNewsController {

    private final StoreNewsService storeNewsService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(summary = "[점주] 소식 등록", description = "가게에 새로운 소식을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "소식 등록 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 가게 아님)"),
            @ApiResponse(responseCode = "404", description = "가게 찾을 수 없음")
    })
    @PostMapping(value = "/stores/{storeId}/news", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Long>> createStoreNews(
            @Parameter(description = "가게 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "소식 이미지 목록") @RequestPart(required = false) List<MultipartFile> images,
            @RequestPart("request") String requestJson) throws IOException, MethodArgumentNotValidException {
        CreateStoreNewsRequest request = objectMapper.readValue(requestJson, CreateStoreNewsRequest.class);
        validateRequest(request);

        Long newsId = storeNewsService.createStoreNews(principalDetails.getUser(), request, storeId, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(newsId));
    }

    @Operation(summary = "[공통] 소식 목록 조회", description = "가게의 소식 목록을 조회합니다.")
    @GetMapping("/stores/{storeId}/news")
    public ResponseEntity<CommonResponse<PageResponse<StoreNewsResponse>>> getStoreNewsList(
            @Parameter(description = "가게 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "페이징 정보 (page, size, sort)") @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<StoreNewsResponse> response = storeNewsService.getStoreNewsList(storeId, pageable,
                principalDetails != null ? principalDetails.getUser() : null);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[공통] 소식 상세 조회", description = "소식 상세 정보를 조회합니다.")
    @GetMapping("/store-news/{newsId}")
    public ResponseEntity<CommonResponse<StoreNewsResponse>> getStoreNews(
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        StoreNewsResponse response = storeNewsService.getStoreNews(newsId,
                principalDetails != null ? principalDetails.getUser() : null);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 소식 수정", description = "소식을 수정합니다.")
    @PatchMapping(value = "/store-news/{newsId}", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Void>> updateStoreNews(
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "변경할 소식 이미지 목록") @RequestPart(required = false) List<MultipartFile> images,
            @RequestPart("request") String requestJson) throws IOException, MethodArgumentNotValidException {
        UpdateStoreNewsRequest request = objectMapper.readValue(requestJson, UpdateStoreNewsRequest.class);
        validateRequest(request);

        storeNewsService.updateStoreNews(newsId, principalDetails.getUser(), request, images);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 소식 삭제", description = "소식을 삭제합니다.")
    @DeleteMapping("/store-news/{newsId}")
    public ResponseEntity<CommonResponse<Void>> deleteStoreNews(
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        storeNewsService.deleteStoreNews(newsId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }

    @Operation(summary = "[공통] 소식 좋아요 토글", description = "소식에 좋아요를 누르거나 취소합니다.")
    @PostMapping("/store-news/{newsId}/likes")
    public ResponseEntity<CommonResponse<Void>> toggleLike(
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        storeNewsService.toggleLike(newsId, principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[공통] 댓글 작성", description = "소식에 댓글을 작성합니다.")
    @PostMapping("/store-news/{newsId}/comments")
    public ResponseEntity<CommonResponse<Long>> createComment(
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid CreateStoreNewsCommentRequest request) {
        Long commentId = storeNewsService.createComment(newsId, principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(commentId));
    }

    @Operation(summary = "[공통] 댓글 목록 조회", description = "소식의 댓글 목록을 조회합니다.")
    @GetMapping("/store-news/{newsId}/comments")
    public ResponseEntity<CommonResponse<PageResponse<StoreNewsCommentResponse>>> getComments(
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<StoreNewsCommentResponse> response = storeNewsService.getComments(newsId, pageable,
                principalDetails != null ? principalDetails.getUser() : null);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[공통] 댓글 삭제", description = "자신의 댓글을 삭제합니다.")
    @DeleteMapping("/store-news/{newsId}/comments/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment(
            @Parameter(description = "소식 ID") @PathVariable Long newsId,
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        storeNewsService.deleteComment(commentId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
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
