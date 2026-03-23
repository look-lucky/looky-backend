package com.looky.domain.organization.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.organization.dto.UniversityResponse;
import com.looky.domain.organization.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public University", description = "대학 공개 조회 API")
@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class PublicUniversityController {

    private final UniversityService universityService;

    @Operation(summary = "[공통] 대학 목록 조회", description = "전체 대학 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<UniversityResponse>>> getUniversities() {
        List<UniversityResponse> responses = universityService.getUniversitiesForAll();
        return ResponseEntity.ok(CommonResponse.success(responses));
    }
}
