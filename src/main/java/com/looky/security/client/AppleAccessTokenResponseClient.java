package com.looky.security.client;

import com.looky.common.util.AppleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleAccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final DefaultAuthorizationCodeTokenResponseClient defaultClient = new DefaultAuthorizationCodeTokenResponseClient();
    private final AppleUtil appleUtil;

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();

        if ("apple".equals(clientRegistration.getRegistrationId())) {
            log.info("Generating Apple Client Secret...");
            String clientSecret = appleUtil.createClientSecret();

            ClientRegistration newClientRegistration = ClientRegistration.withClientRegistration(clientRegistration)
                    .clientSecret(clientSecret)
                    .build();

            authorizationGrantRequest = new OAuth2AuthorizationCodeGrantRequest(
                    newClientRegistration,
                    authorizationGrantRequest.getAuthorizationExchange()
            );
        }

        return defaultClient.getTokenResponse(authorizationGrantRequest);
    }
}
