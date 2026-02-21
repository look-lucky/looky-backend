package com.looky.domain.partnership.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.partnership.dto.StorePartnershipResponse;
import com.looky.domain.partnership.service.PartnershipService;
import com.looky.security.details.PrincipalDetails;
import com.looky.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Partnership", description = "제휴 혜택 관련 API")
@RestController
@RequestMapping("/api/stores/{storeId}/partnerships")
@RequiredArgsConstructor
public class PartnershipController {

    private final PartnershipService partnershipService;

    @Operation(summary = "[학생] 특정 상점의 제휴 혜택 목록 조회", description = "특정 상점이 맺고 있는 전체 제휴 혜택과 내가 받을 수 있는 혜택인지 여부를 함께 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "상점 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<StorePartnershipResponse>>> getStorePartnerships(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        User user = principalDetails != null ? principalDetails.getUser() : null;
        List<StorePartnershipResponse> response = partnershipService.getStorePartnerships(storeId, user);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
