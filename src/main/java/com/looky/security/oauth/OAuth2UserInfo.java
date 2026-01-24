package com.looky.security.oauth;

public interface OAuth2UserInfo {
    String getProviderId(); // 소셜 식별자 (google_sub, kakao_id 등)

    String getProvider(); // google, kakao

    String getEmail();

    String getName();
}
