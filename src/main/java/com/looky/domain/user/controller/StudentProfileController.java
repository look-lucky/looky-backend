package com.looky.domain.user.controller;

import com.looky.common.response.CommonResponse;
import com.looky.common.response.SwaggerErrorResponse;
import com.looky.domain.user.dto.StudentInfoResponse;
import com.looky.domain.user.dto.UpdateStudentProfileRequest;
import com.looky.domain.user.dto.UpdateUniversityRequest;
import com.looky.domain.user.service.UserProfileService;
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

@Tag(name = "Student Profile", description = "학생 프로필 API")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "[학생] 내 정보 조회", description = "학생의 대학, 단과대, 학과, 동아리 활동 여부를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "학생 회원이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "학생 프로필 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<StudentInfoResponse>> getStudentInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(CommonResponse.success(userProfileService.getStudentInfoForStudent(principalDetails.getUser().getId())));
    }

    @Operation(summary = "[학생] 프로필 수정", description = "학생의 프로필(닉네임, 단과대, 학과, 동아리 가입여부)을 수정합니다. 대학 변경은 별도 API 사용.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 조직 카테고리 요청", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "학생 회원이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PutMapping("/profile")
    public ResponseEntity<CommonResponse<Void>> updateStudentProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UpdateStudentProfileRequest request) {
        userProfileService.updateStudentProfileForStudent(principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "[학생] 대학 변경", description = "학생의 소속 대학을 변경합니다. (기존 단과대/학과는 초기화됨)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대학 변경 성공"),
            @ApiResponse(responseCode = "403", description = "학생 회원이 아님", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "대학 정보 없음", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping("/profile/university")
    public ResponseEntity<CommonResponse<Void>> updateUniversity(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UpdateUniversityRequest request) {
        userProfileService.updateUniversityForStudent(principalDetails.getUser().getId(), request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
