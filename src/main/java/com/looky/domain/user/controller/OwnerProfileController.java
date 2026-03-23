package com.looky.domain.user.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.user.dto.OwnerInfoResponse;
import com.looky.domain.user.service.UserProfileService;
import com.looky.security.details.PrincipalDetails;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Owner Profile", description = "점주 프로필 API")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "[점주] 내 정보 조회", description = "점주의 이름, 이메일, 아이디, 성별, 생년월일을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "점주 회원이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "점주 프로필 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<OwnerInfoResponse>> getOwnerInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(CommonResponse.success(userProfileService.getOwnerInfoForOwner(principalDetails.getUser().getId())));
    }
}
