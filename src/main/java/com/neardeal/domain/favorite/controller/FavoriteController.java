package com.neardeal.domain.favorite.controller;

import com.neardeal.common.response.CommonResponse;
import com.neardeal.common.response.PageResponse;
import com.neardeal.common.response.SwaggerErrorResponse;
import com.neardeal.domain.favorite.dto.FavoriteStoreResponse;
import com.neardeal.domain.favorite.service.FavoriteService;
import com.neardeal.security.details.PrincipalDetails;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Favorite", description = "단골 상점(즐겨찾기) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FavoriteController {

        private final FavoriteService favoriteService;

        @Operation(summary = "[학생] 상점 즐겨찾기 추가", description = "특정 상점을 단골 상점으로 등록합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "즐겨찾기 추가 성공"),
                        @ApiResponse(responseCode = "400", description = "본인 상점", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "이미 추가된 상점", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/stores/{storeId}/favorites")
        public ResponseEntity<CommonResponse<Void>> addFavorite(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @Parameter(description = "상점 ID") @PathVariable Long storeId
        )
        {
                favoriteService.addFavorite(principalDetails.getUser(), storeId);
                return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(null));
        }

        @Operation(summary = "[학생] 상점 즐겨찾기 취소", description = "단골 상점 등록을 취소합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "즐겨찾기 취소 성공"),
                        @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @DeleteMapping("/stores/{storeId}/favorites")
        public ResponseEntity<CommonResponse<Void>> removeFavorite(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @Parameter(description = "상점 ID") @PathVariable Long storeId
        )
        {
                favoriteService.removeFavorite(principalDetails.getUser(), storeId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
        }

        @Operation(summary = "[공통] 상점 즐겨찾기 수 조회", description = "특정 상점의 총 단골 수를 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @GetMapping("/stores/{storeId}/favorites/count")
        public ResponseEntity<CommonResponse<Long>> countFavorites(
                @Parameter(description = "상점 ID") @PathVariable Long storeId
        )
        {
                Long count = favoriteService.countFavorites(storeId);
                return ResponseEntity.ok(CommonResponse.success(count));
        }

        @Operation(summary = "[학생] 내 단골 상점 목록 조회", description = "내가 등록한 단골 상점 목록을 페이징하여 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공")
        })
        @GetMapping("/favorites")
        public ResponseEntity<CommonResponse<PageResponse<FavoriteStoreResponse>>> getMyFavorites(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @Parameter(description = "페이징 정보") @PageableDefault(size = 10) Pageable pageable
        )
        {
                Page<FavoriteStoreResponse> favorites = favoriteService.getMyFavorites(principalDetails.getUser(),
                                pageable);
                return ResponseEntity.ok(CommonResponse.success(PageResponse.from(favorites)));
        }
}
