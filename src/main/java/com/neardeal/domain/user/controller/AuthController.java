package com.neardeal.domain.user.controller;

import com.neardeal.common.response.CommonResponse;
import com.neardeal.common.response.SwaggerErrorResponse;
import com.neardeal.common.util.CookieUtil;
import com.neardeal.domain.user.dto.*;
import com.neardeal.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final CookieUtil cookieUtil;

        @Operation(summary = "[학생] 학생 회원가입", description = "학생 회원을 등록합니다.")
        @PostMapping("/signup/student")
        public ResponseEntity<CommonResponse<Long>> signupStudent(@RequestBody StudentSignupRequest request) {
            Long id = authService.signupStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(id));
        }

        @Operation(summary = "[점주] 점주 회원가입", description = "점주 회원을 등록합니다.")
        @PostMapping("/signup/owner")
        public ResponseEntity<CommonResponse<Long>> signupOwner(@RequestBody OwnerSignupRequest request) {
            Long id = authService.signupOwner(request);
            return ResponseEntity.ok(CommonResponse.success(id));
        }
        
        @Operation(summary = "[학생회] 학생회 회원가입", description = "학생회 회원을 등록합니다.")
        @PostMapping("/signup/council")
        public ResponseEntity<CommonResponse<Long>> signupcouncil(@RequestBody CouncilSignupRequest request) {
                Long id = authService.signupCouncil(request);
                return ResponseEntity.ok(CommonResponse.success(id));
        }

        @Operation(summary = "[공통] 로그인", description = "아이디와 비밀번호로 로그인합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "로그인 성공"),
                @ApiResponse(responseCode = "400", description = "로그인 실패 (아이디/비밀번호 불일치)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/login")
        public ResponseEntity<CommonResponse<LoginResponse>> login(
                @RequestBody LoginRequest request
        )
        {
                AuthTokens authTokens = authService.login(request);

                ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(authTokens.getRefreshToken());

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                .body(CommonResponse.success(LoginResponse.of(authTokens.getAccessToken(),
                                                authTokens.getExpiresIn())));
        }

        @Operation(summary = "[공통] 토큰 갱신", description = "RefreshToken으로 AccessToken을 갱신합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
                @ApiResponse(responseCode = "401", description = "유효하지 않은 RefreshToken", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/refresh")
        public ResponseEntity<CommonResponse<LoginResponse>> refresh(
                @CookieValue(value = "refreshToken", required = false) String refreshToken
        )
        {
                AuthTokens authTokens = authService.refresh(refreshToken);

                // RRT 적용
                ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(authTokens.getRefreshToken());

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                .body(CommonResponse.success(LoginResponse.of(authTokens.getAccessToken(),
                                                authTokens.getExpiresIn())));
        }

        @Operation(summary = "[공통] 로그아웃", description = "사용자를 로그아웃 처리합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "로그아웃 성공")
        })
        @PostMapping("/logout")
        public ResponseEntity<CommonResponse<Void>> logout(
                @CookieValue(value = "refreshToken", required = false) String refreshToken
        )
        {
                authService.logout(refreshToken);

                ResponseCookie deleteCookie = cookieUtil.createExpiredCookie("refreshToken");

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                                .body(CommonResponse.success(null));
        }

        @Operation(summary = "[공통] 소셜 회원가입 완료", description = "소셜 로그인 후 추가 정보를 입력하여 회원가입을 완료합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "회원가입 완료 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "409", description = "이미 존재하는 소셜 정보", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @PostMapping("/complete-social-signup")
        public ResponseEntity<CommonResponse<LoginResponse>> completeSocialSignup(
                Long userId,
                CompleteSocialSignupRequest request
        )
        {
                AuthTokens authTokens = authService.completeSocialSignup(userId, request);

                ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(authTokens.getRefreshToken());

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                        .body(CommonResponse.success(LoginResponse.of(authTokens.getAccessToken(), authTokens.getExpiresIn())));
        }
}