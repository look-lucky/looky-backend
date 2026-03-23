package com.looky.domain.user.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.user.dto.ChangePasswordRequest;
import com.looky.domain.user.dto.ChangeUsernameRequest;
import com.looky.domain.user.service.AccountService;
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

@Tag(name = "Account", description = "계정 설정 API")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "[공통] 아이디 변경", description = "사용자의 아이디를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아이디 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/change-username")
    public ResponseEntity<CommonResponse<Void>> changeUsername(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid ChangeUsernameRequest request) {
        accountService.changeUsername(principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[공통] 비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/change-password")
    public ResponseEntity<CommonResponse<Void>> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid ChangePasswordRequest request) {
        accountService.changePassword(principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
