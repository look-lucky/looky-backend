package com.looky.domain.store.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.store.dto.UpdateMenuBoardImagesRequest;
import com.looky.domain.store.service.MenuBoardImageService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MenuBoardImage", description = "메뉴판 이미지 관련 API")
@RestController
@RequestMapping("/api/stores/{storeId}/menu-board-images")
@RequiredArgsConstructor
public class MenuBoardImageController {

    private final MenuBoardImageService menuBoardImageService;

    @Operation(summary = "[공통] 메뉴판 이미지 목록 조회", description = "특정 상점의 메뉴판 이미지 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<String>>> getMenuBoardImages(
            @Parameter(description = "상점 ID") @PathVariable Long storeId
    ) {
        List<String> imageUrls = menuBoardImageService.getMenuBoardImages(storeId);
        return ResponseEntity.ok(CommonResponse.success(imageUrls));
    }

    @Operation(summary = "[점주] 메뉴판 이미지 수정", description = "메뉴판 이미지 목록 전체를 교체합니다. 빈 배열 전송 시 전체 삭제. (본인 상점만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴판 이미지 수정 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 10장 초과", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<CommonResponse<Void>> updateMenuBoardImages(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @RequestBody @Valid UpdateMenuBoardImagesRequest request
    ) {
        menuBoardImageService.updateMenuBoardImages(storeId, principalDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
