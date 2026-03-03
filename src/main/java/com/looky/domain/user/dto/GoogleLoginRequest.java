package com.looky.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "구글 소셜 로그인 요청 (앱 네이티브)")
public class GoogleLoginRequest {

    @NotBlank(message = "Google id_token은 필수입니다.")
    @Schema(description = "Google 인증 후 발급받은 JWT 형식의 id_token", example = "eyJhbGciOiJSUzI1NiIs...")
    private String idToken;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }
}
