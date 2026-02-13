package com.looky.domain.user.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.common.util.CookieUtil;
import com.looky.domain.user.dto.*;
import com.looky.domain.user.service.AuthService;
import com.looky.domain.user.service.EmailVerificationService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final EmailVerificationService emailVerificationService;
        private final CookieUtil cookieUtil;

        @Operation(summary = "[학생] 학생 회원가입", description = "학생 회원을 등록합니다.")
        @PostMapping("/signup/student")
        public ResponseEntity<CommonResponse<LoginResponse>> signupStudent(@RequestBody StudentSignupRequest request) {
            AuthTokens authTokens = authService.signupStudent(request);
            ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(authTokens.getRefreshToken());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(CommonResponse.success(LoginResponse.of(authTokens.getAccessToken(), authTokens.getExpiresIn())));
        }

        @Operation(summary = "[점주] 점주 회원가입", description = "점주 회원을 등록합니다.")
        @PostMapping("/signup/owner")
        public ResponseEntity<CommonResponse<LoginResponse>> signupOwner(@RequestBody OwnerSignupRequest request) {
            AuthTokens authTokens = authService.signupOwner(request);
            ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(authTokens.getRefreshToken());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(CommonResponse.success(LoginResponse.of(authTokens.getAccessToken(), authTokens.getExpiresIn())));
        }
        
        @Operation(summary = "[학생회] 학생회 회원가입", description = "학생회 회원을 등록합니다.")
        @PostMapping("/signup/council")
        public ResponseEntity<CommonResponse<LoginResponse>> signupcouncil(@RequestBody CouncilSignupRequest request) {
                AuthTokens authTokens = authService.signupCouncil(request);
                ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(authTokens.getRefreshToken());

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                        .body(CommonResponse.success(LoginResponse.of(authTokens.getAccessToken(), authTokens.getExpiresIn())));
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

        @Operation(summary = "[공통] 회원 탈퇴", description = "회원을 탈퇴 처리합니다. (Soft Delete)")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 (기타 사유 미입력 등)", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
        })
        @DeleteMapping("/withdraw")
        public ResponseEntity<CommonResponse<Void>> withdraw(
                @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
                @RequestBody @Valid WithdrawRequest request
        ) {
            authService.withdraw(principalDetails.getUser(), request);
            
            // 리프레시 토큰 쿠키 삭제
            ResponseCookie deleteCookie = cookieUtil.createExpiredCookie("refreshToken");

            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body(CommonResponse.success(null));
        }

        @Operation(summary = "[공통] 아이디 중복 확인", description = "아이디 사용 가능 여부를 확인합니다. (true: 사용 가능, false: 중복)")
        @GetMapping("/check-username")
        public ResponseEntity<CommonResponse<Boolean>> checkUsernameAvailability(@RequestParam String username) {
                return ResponseEntity.ok(CommonResponse.success(authService.checkUsernameAvailability(username)));
        }

        @Operation(summary = "[공통] 회원 가입용 이메일 인증 발송", description = "회원 가입용 이메일 인증 코드를 전송합니다. (universityId 존재 시 도메인 검사 진행)")
        @PostMapping("email/send-code")
        public ResponseEntity<CommonResponse<Void>> send(@Valid @RequestBody SendEmailCodeRequest request) {
                log.info("email verification request received: email={}, universityId={}", request.getEmail(), request.getUniversityId());
                emailVerificationService.sendCode(request.getEmail(), request.getUniversityId());
                return ResponseEntity.ok(CommonResponse.success(null));
        }
        
        @Operation(summary = "[공통] 회원 가입용 이메일 인증 확인", description = "회원 가입용 이메일 인증 코드를 검증합니다. (true: 검증 (일치) 완료, false: 검증 실패 (코드 불일치 및 이미 등록된 이메일)")
        @PostMapping("email/verify")
        public ResponseEntity<CommonResponse<Void>> verify(@Valid @RequestBody VerifyEmailCodeRequest request) {
                log.info("email verification check request received: email={}, code={}", request.getEmail(), request.getCode());
                emailVerificationService.verifyCode(request.getEmail(), request.getCode());
                return ResponseEntity.ok(CommonResponse.success(null));
        }

        @Operation(summary = "[공통] 아이디 찾기 - 인증번호 발송", description = "가입된 이메일로 인증번호를 발송합니다.")
        @PostMapping("/find-id/send-code")
        public ResponseEntity<CommonResponse<Void>> sendCodeForFindId(@RequestBody @Valid SendEmailCodeRequest request) {
            authService.sendVerificationCodeForAccountRecovery(request.getEmail());
            return ResponseEntity.ok(CommonResponse.success(null));
        }

        @Operation(summary = "[공통] 아이디 찾기 - 인증 확인 및 아이디 반환", description = "인증번호 검증 후 가입된 아이디를 반환합니다.")
        @PostMapping("/find-id/verify")
        public ResponseEntity<CommonResponse<String>> verifyCodeForFindId(@RequestBody @Valid VerifyEmailCodeRequest request) {
            String username = authService.findUsernameByEmail(request.getEmail(), request.getCode());
            return ResponseEntity.ok(CommonResponse.success(username));
        }

        @Operation(summary = "[공통] 비밀번호 찾기 - 인증번호 발송", description = "아이디와 이메일이 일치하는지 확인 후 인증번호를 발송합니다.")
        @PostMapping("/find-password/send-code")
        public ResponseEntity<CommonResponse<Void>> sendCodeForFindPassword(@RequestBody @Valid FindPasswordSendCodeRequest request) {
            authService.sendVerificationCodeForPasswordReset(request.getUsername(), request.getEmail());
            return ResponseEntity.ok(CommonResponse.success(null));
        }

        @Operation(summary = "[공통] 비밀번호 재설정 - 인증 확인 및 임시 토큰 반환", description = "인증번호 검증 후 비밀번호 재설정을 위한 임시 토큰을 반환합니다.")
        @PostMapping("/find-password/verify")
        public ResponseEntity<CommonResponse<String>> verifyCodeForFindPassword(@RequestBody @Valid VerifyEmailCodeRequest request) {
            // 비밀번호 재설정 토큰 반환
            String resetToken = authService.verifyCodeForPasswordReset(request.getEmail(), request.getCode());
            return ResponseEntity.ok(CommonResponse.success(resetToken));
        }

        @Operation(summary = "[공통] 비밀번호 재설정", description = "비밀번호 재설정 토큰을 사용하여 새 비밀번호로 변경합니다.")
        @PostMapping("/find-password/reset")
        public ResponseEntity<CommonResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
            authService.resetPassword(request.getResetToken(), request.getNewPassword());
            return ResponseEntity.ok(CommonResponse.success(null));
        }
}