package com.looky.domain.user.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.PageResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.user.dto.AdminUserResponse;
import com.looky.domain.user.dto.UserRoleUpdateRequest;
import com.looky.domain.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin User", description = "관리자 사용자 관리 API")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "[관리자] 전체 사용자 목록 조회", description = "가입된 모든 사용자를 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<AdminUserResponse>>> getAllUsers(
            @Parameter(description = "페이징 정보") @PageableDefault(size = 10) Pageable pageable
    ) {
        PageResponse<AdminUserResponse> response = adminUserService.getAllUsersForAdmin(pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[관리자] 사용자 권한 수정", description = "사용자의 권한을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PatchMapping("/{userId}/role")
    public ResponseEntity<CommonResponse<Void>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody @Valid UserRoleUpdateRequest request
    ) {
        adminUserService.updateUserRoleForAdmin(userId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
