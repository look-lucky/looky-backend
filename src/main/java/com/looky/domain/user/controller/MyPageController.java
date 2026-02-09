package com.looky.domain.user.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.user.dto.ChangePasswordRequest;
import com.looky.domain.user.dto.ChangeUsernameRequest;
import com.looky.domain.user.dto.UpdateStudentProfileRequest;
import com.looky.domain.user.dto.UpdateUniversityRequest;
import com.looky.domain.user.service.MyPageService;
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
import com.looky.domain.user.dto.StudentInfoResponse;

@Tag(name = "MyPage", description = "마이페이지 관련 API")
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

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
        myPageService.changeUsername(principalDetails.getUser().getId(), request);
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
        myPageService.changePassword(principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[학생] 내 정보 조회", description = "학생의 대학, 단과대, 학과, 동아리 활동 여부를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "학생 회원이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "학생 프로필 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/student/profile")
    public ResponseEntity<CommonResponse<StudentInfoResponse>> getStudentInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(CommonResponse.success(myPageService.getStudentInfo(principalDetails.getUser().getId())));
    }

    @Operation(summary = "[학생] 프로필 수정", description = "학생의 프로필(닉네임, 단과대, 학과, 동아리 가입여부)을 수정합니다. 대학 변경은 별도 API 사용.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "403", description = "학생 회원이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 조직 카테고리 요청", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PutMapping("/student/profile")
    public ResponseEntity<CommonResponse<Void>> updateStudentProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UpdateStudentProfileRequest request) {
        myPageService.updateStudentProfile(principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[학생] 대학 변경", description = "학생의 소속 대학을 변경합니다. (기존 단과대/학과는 초기화됨)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대학 변경 성공"),
            @ApiResponse(responseCode = "403", description = "학생 회원이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "대학 정보 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/student/university")
    public ResponseEntity<CommonResponse<Void>> updateUniversity(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UpdateUniversityRequest request) {
        myPageService.updateUniversity(principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
