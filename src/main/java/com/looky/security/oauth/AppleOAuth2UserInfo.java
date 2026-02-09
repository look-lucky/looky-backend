package com.looky.security.oauth;

import java.util.Map;

public class AppleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "apple";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        // Apple id_token에는 name 정보가 포함되어 있지= 않음
        return null;
    }
}
