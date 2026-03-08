package com.looky.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "애플 소셜 로그인 요청 (앱 네이티브)")
public class AppleLoginRequest {

    @NotBlank(message = "Apple id_token은 필수입니다.")
    @Schema(description = "Apple 인증 후 발급받은 JWT 형식의 id_token", example = "eyJraWQiOiI... (긴 문자열)")
    private String idToken;

    @Schema(description = "유저의 이름 (Apple 로그인 최초 1회에만 제공됨. 없을 경우 null)", example = "홍길동")
    private String name;

    public AppleLoginRequest(String idToken, String name) {
        this.idToken = idToken;
        this.name = name;
    }
}
