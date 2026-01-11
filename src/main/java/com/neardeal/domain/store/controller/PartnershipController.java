package com.neardeal.domain.store.controller;

import com.neardeal.common.response.CommonResponse;
import com.neardeal.common.response.SwaggerErrorResponse;
import com.neardeal.domain.store.dto.CreatePartnershipRequest;
import com.neardeal.domain.store.dto.UpdatePartnershipRequest;
import com.neardeal.domain.store.service.PartnershipService;
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

@Tag(name = "Partnership", description = "제휴 관련 API")
@RestController
@RequestMapping("/api/stores/{storeId}/partnerships")
@RequiredArgsConstructor
public class PartnershipController {

    private final PartnershipService partnershipService;

    @Operation(summary = "[점주] 제휴 등록", description = "특정 조직에 제휴를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "제휴 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상점 또는 조직 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 등록된 제휴", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createPartnership(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid CreatePartnershipRequest request
    )
    {
        Long partnershipId = partnershipService.createPartnership(storeId, principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(partnershipId));
    }

    @Operation(summary = "[점주] 제휴 혜택 수정", description = "제휴 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제휴 혜택 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 상점 아님)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "제휴 정보 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/{partnershipId}")
    public ResponseEntity<CommonResponse<Void>> updatePartnershipBenefit(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(description = "제휴 ID") @PathVariable Long partnershipId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UpdatePartnershipRequest request
    )
    {
        partnershipService.updatePartnershipBenefit(storeId, partnershipId, principalDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[점주] 제휴 삭제", description = "제휴를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "제휴 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "제휴 정보 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @DeleteMapping("/{partnershipId}")
    public ResponseEntity<CommonResponse<Void>> deletePartnership(
            @Parameter(description = "상점 ID") @PathVariable Long storeId,
            @Parameter(description = "제휴 ID") @PathVariable Long partnershipId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    )
    {
        partnershipService.deletePartnership(storeId, partnershipId, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
    }
}
