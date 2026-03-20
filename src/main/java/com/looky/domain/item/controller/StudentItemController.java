package com.looky.domain.item.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.item.dto.ItemResponse;
import com.looky.domain.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Item", description = "학생 상품 조회 API")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentItemController {

    private final ItemService itemService;

    @Operation(summary = "[학생] 상점별 상품 목록 조회", description = "특정 상점의 모든 상품을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/stores/{storeId}/items")
    public ResponseEntity<CommonResponse<List<ItemResponse>>> getItems(
            @Parameter(description = "상점 ID") @PathVariable Long storeId
    ) {
        List<ItemResponse> response = itemService.getItemsForStudent(storeId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[학생] 상품 단건 조회", description = "상품 ID로 상품의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/items/{itemId}")
    public ResponseEntity<CommonResponse<ItemResponse>> getItem(
            @Parameter(description = "상품 ID") @PathVariable Long itemId
    ) {
        ItemResponse response = itemService.getItemForStudent(itemId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
