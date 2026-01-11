package com.neardeal.domain.item.controller;

import com.neardeal.common.response.CommonResponse;
import com.neardeal.common.response.SwaggerErrorResponse;
import com.neardeal.domain.item.dto.CreateItemRequest;
import com.neardeal.domain.item.dto.ItemResponse;
import com.neardeal.domain.item.dto.UpdateItemRequest;
import com.neardeal.domain.item.service.ItemService;
import com.neardeal.security.details.PrincipalDetails;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Item", description = "상품 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemController {

        private final ItemService itemService;

        @Operation(summary = "[점주] 상품 등록", description = "상점에 새로운 상품을 등록합니다. (본인 상점만 가능)")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "상품 등록 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/stores/{storeId}/items")
        public ResponseEntity<CommonResponse<Long>> createItem(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @Parameter(description = "상품 ID") @PathVariable Long storeId,
                @Parameter(description = "상품 이미지") @RequestPart MultipartFile image,
                @RequestPart @Valid CreateItemRequest request
        ) throws IOException {
                Long itemId = itemService.createItem(storeId, principalDetails.getUser(), request, image);
                return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(itemId));
        }

        @Operation(summary = "[공통] 상점별 상품 목록 조회", description = "특정 상점의 모든 상품을 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "성공"),
                @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/stores/{storeId}/items")
        public ResponseEntity<CommonResponse<List<ItemResponse>>> getItems (
                @Parameter(description = "상점 ID") @PathVariable Long storeId
        )
        {
                List<ItemResponse> response = itemService.getItems(storeId);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[공통] 상품 단건 조회", description = "상품 ID로 상품의 상세 정보를 조회합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "성공"),
                @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/items/{itemId}")
        public ResponseEntity<CommonResponse<ItemResponse>> getItem(
                @Parameter(description = "상품 ID") @PathVariable Long itemId
        )
        {
                ItemResponse response = itemService.getItem(itemId);
                return ResponseEntity.ok(CommonResponse.success(response));
        }

        @Operation(summary = "[점주] 상품 수정", description = "상품 정보를 수정합니다. (본인 상점만 가능)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "상품 수정 성공"),
                        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PatchMapping("/items/{itemId}")
        public ResponseEntity<CommonResponse<Void>> updateItem(
                @Parameter(description = "상품 ID") @PathVariable Long itemId,
                @Parameter(description = "변경할 상품 이미지") @RequestPart MultipartFile image,
                @RequestPart @Valid UpdateItemRequest request,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        ) throws IOException {

                itemService.updateItem(itemId, principalDetails.getUser(), request, image);

                return ResponseEntity.ok(CommonResponse.success(null));
        }

        @Operation(summary = "[점주] 상품 삭제", description = "상품을 삭제합니다. (본인 상점만 가능)")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "상품 삭제 성공"),
                @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @DeleteMapping("/items/{itemId}")
        public ResponseEntity<CommonResponse<Void>> deleteItem(
                @Parameter(description = "상품 ID") @PathVariable Long itemId,
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
        )
        {
                itemService.deleteItem(itemId, principalDetails.getUser());
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
        }
}
