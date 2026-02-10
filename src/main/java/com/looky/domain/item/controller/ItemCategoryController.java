package com.looky.domain.item.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.item.dto.ItemCategoryResponse;
import com.looky.domain.item.service.ItemCategoryService;
import com.looky.security.details.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "ItemCategory", description = "상품 카테고리 관련 API")
@RestController
@RequestMapping("/api/stores/{storeId}/item-categories")
@RequiredArgsConstructor
public class ItemCategoryController {

    private final ItemCategoryService itemCategoryService;

    @Operation(summary = "[점주] 상품 카테고리 등록", description = "매장에 새로운 상품 카테고리를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "카테고리 등록 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "매장 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createItemCategory(
            @PathVariable Long storeId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody Map<String, String> request
    ) {
        Long categoryId = itemCategoryService.createItemCategory(storeId, principalDetails.getUser(), request.get("name"));
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(categoryId));
    }

    @Operation(summary = "[공통] 상품 카테고리 목록 조회", description = "매장의 상품 카테고리 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<List<ItemCategoryResponse>>> getItemCategories(
            @PathVariable Long storeId
    ) {
        List<ItemCategoryResponse> categories = itemCategoryService.getItemCategories(storeId);
        return ResponseEntity.ok(CommonResponse.success(categories));
    }

    @Operation(summary = "[점주] 상품 카테고리 수정", description = "상품 카테고리 이름을 수정합니다.")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<CommonResponse<Void>> updateItemCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody Map<String, String> request
    ) {
        itemCategoryService.updateItemCategory(storeId, categoryId, principalDetails.getUser(), request.get("name"));
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 상품 카테고리 삭제", description = "상품 카테고리를 삭제합니다. (사용 중인 상품이 있으면 삭제 불가)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "409", description = "상품이 연결되어 있어 삭제 불가", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<CommonResponse<Void>> deleteItemCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        itemCategoryService.deleteItemCategory(storeId, categoryId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
