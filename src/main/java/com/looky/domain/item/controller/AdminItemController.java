package com.looky.domain.item.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.item.dto.CreateItemRequest;
import com.looky.domain.item.dto.ItemResponse;
import com.looky.domain.item.dto.UpdateItemRequest;
import com.looky.domain.item.service.ItemService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Item", description = "관리자 상품 관리 API (UNCLAIMED 가게 한정)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemService itemService;

    @Operation(summary = "[관리자] 상품 등록", description = "UNCLAIMED 상태의 가게에 상품을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/stores/{storeId}/items")
    public ResponseEntity<CommonResponse<Long>> createItem(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @RequestBody @Valid CreateItemRequest request
    ) {
        Long itemId = itemService.createItemForAdmin(storeId, principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(itemId));
    }

    @Operation(summary = "[관리자] 상점별 상품 목록 조회", description = "특정 상점의 모든 상품을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}/items")
    public ResponseEntity<CommonResponse<List<ItemResponse>>> getItems(
            @Parameter(description = "상점 ID") @PathVariable Long storeId
    ) {
        List<ItemResponse> response = itemService.getItemsForAdmin(storeId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[관리자] 상품 단건 조회", description = "상품 ID로 상품의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/items/{itemId}")
    public ResponseEntity<CommonResponse<ItemResponse>> getItem(
            @Parameter(description = "상품 ID") @PathVariable Long itemId
    ) {
        ItemResponse response = itemService.getItemForAdmin(itemId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[관리자] 상품 수정", description = "UNCLAIMED 상태의 가게 상품을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CommonResponse<Void>> updateItem(
            @Parameter(description = "상품 ID") @PathVariable Long itemId,
            @RequestBody @Valid UpdateItemRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        itemService.updateItemForAdmin(itemId, principalDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[관리자] 상품 삭제", description = "UNCLAIMED 상태의 가게 상품을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "상품 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CommonResponse<Void>> deleteItem(
            @Parameter(description = "상품 ID") @PathVariable Long itemId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        itemService.deleteItemForAdmin(itemId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
