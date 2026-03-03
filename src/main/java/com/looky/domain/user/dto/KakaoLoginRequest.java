package com.looky.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "카카오 소셜 로그인 요청 (앱 네이티브)")
public class KakaoLoginRequest {

    @NotBlank(message = "Kakao Access Token은 필수입니다.")
    @Schema(description = "Kakao SDK를 통해 발급받은 Access Token", example = "abc123def456...")
    private String accessToken;

    @Schema(description = "Kakao SDK를 통해 발급받은 ID Token (선택)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String idToken;

    public KakaoLoginRequest(String accessToken, String idToken) {
        this.accessToken = accessToken;
        this.idToken = idToken;
    }
}
