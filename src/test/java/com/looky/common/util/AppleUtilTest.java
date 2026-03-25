package com.looky.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AppleUtil 단위 테스트")
class AppleUtilTest {

    // 테스트용 상수
    private static final String TEST_TEAM_ID   = "TEST_TEAM_ID";
    private static final String TEST_CLIENT_ID = "com.looky.test";
    private static final String TEST_KEY_ID    = "TEST_KEY_ID";
    private AppleUtil appleUtil;
    private ECPublicKey testPublicKey;   // JWT 검증용

    @BeforeEach
    void setUp() throws Exception {
        // EC 키페어 생성 (Apple 이 사용하는 ES256 = P-256)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair keyPair = kpg.generateKeyPair();
        testPublicKey = (ECPublicKey) keyPair.getPublic();
        ECPrivateKey testPrivateKey = (ECPrivateKey) keyPair.getPrivate();

        appleUtil = new AppleUtil() {
            @Override
            protected java.security.PrivateKey getPrivateKeyForTest() {
                return testPrivateKey;
            }
        };

        // @Value 필드 주입
        ReflectionTestUtils.setField(appleUtil, "teamId",        TEST_TEAM_ID);
        ReflectionTestUtils.setField(appleUtil, "clientId",      TEST_CLIENT_ID);
        ReflectionTestUtils.setField(appleUtil, "keyId",         TEST_KEY_ID);
        ReflectionTestUtils.setField(appleUtil, "privateKeyPath", "classpath:dummy.p8");
    }

    @Nested
    @DisplayName("createClientSecret — JWT 구조 검증")
    class CreateClientSecret {

        private Claims parsedClaims;
        private String jwt;

        @BeforeEach
        void parseJwt() {
            jwt = appleUtil.createClientSecret();
            parsedClaims = Jwts.parser()
                    .verifyWith(testPublicKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        }

        @Test
        @DisplayName("JWT 가 null 이나 빈 문자열이 아니어야 한다")
        void JWT가_null이_아니다() {
            assertThat(jwt).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("JWT 형식이 header.payload.signature 세 파트여야 한다")
        void JWT_세파트_구조() {
            assertThat(jwt.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("issuer(iss) 가 teamId 와 같아야 한다")
        void issuer가_teamId() {
            assertThat(parsedClaims.getIssuer()).isEqualTo(TEST_TEAM_ID);
        }

        @Test
        @DisplayName("subject(sub) 가 clientId 와 같아야 한다")
        void subject가_clientId() {
            assertThat(parsedClaims.getSubject()).isEqualTo(TEST_CLIENT_ID);
        }

        @Test
        @DisplayName("audience(aud) 에 https://appleid.apple.com 이 포함되어야 한다")
        void audience에_apple_포함() {
            assertThat(parsedClaims.getAudience())
                    .contains("https://appleid.apple.com");
        }

        @Test
        @DisplayName("issuedAt(iat) 이 현재 시각 기준 과거여야 한다")
        void issuedAt이_현재보다_과거() {
            assertThat(parsedClaims.getIssuedAt()).isBefore(new Date());
        }

        @Test
        @DisplayName("expiration(exp) 이 issuedAt 보다 미래여야 한다")
        void expiration이_issuedAt보다_미래() {
            assertThat(parsedClaims.getExpiration())
                    .isAfter(parsedClaims.getIssuedAt());
        }

        @Test
        @DisplayName("만료 시간이 현재로부터 약 5분 이내여야 한다")
        void 만료시간이_5분이내() {
            Date now = new Date();
            Date exp = parsedClaims.getExpiration();
            long diffSeconds = (exp.getTime() - now.getTime()) / 1000;

            assertThat(diffSeconds).isBetween(295L, 305L);
        }

        @Test
        @DisplayName("header 의 kid 가 keyId 와 같아야 한다")
        void header_kid가_keyId() {
            String headerJson = new String(
                    java.util.Base64.getUrlDecoder().decode(jwt.split("\\.")[0])
            );
            assertThat(headerJson).contains("\"kid\":\"" + TEST_KEY_ID + "\"");
        }

        @Test
        @DisplayName("서명 알고리즘이 ES256 이어야 한다")
        void 서명_알고리즘이_ES256() {
            String headerJson = new String(
                    java.util.Base64.getUrlDecoder().decode(jwt.split("\\.")[0])
            );
            assertThat(headerJson).contains("\"alg\":\"ES256\"");
        }

        @Test
        @DisplayName("연속 호출 시 매번 새로운 JWT 를 생성한다 (iat 차이)")
        void 연속호출시_새로운_JWT() throws InterruptedException {
            String first = appleUtil.createClientSecret();
            Thread.sleep(1100);
            String second = appleUtil.createClientSecret();
            assertThat(first).isNotEqualTo(second);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // getPrivateKey — 파일 로드 실패 시나리오 (별도 인스턴스로 검증)
    // ────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPrivateKey — 파일 로드 실패 시나리오")
    class GetPrivateKeyFailure {

        @Test
        @DisplayName("존재하지 않는 키 파일 경로이면 RuntimeException 이 발생한다")
        void 잘못된_키경로면_RuntimeException() {
            AppleUtil brokenUtil = new AppleUtil();
            ReflectionTestUtils.setField(brokenUtil, "teamId",        TEST_TEAM_ID);
            ReflectionTestUtils.setField(brokenUtil, "clientId",      TEST_CLIENT_ID);
            ReflectionTestUtils.setField(brokenUtil, "keyId",         TEST_KEY_ID);
            ReflectionTestUtils.setField(brokenUtil, "privateKeyPath", "classpath:nonexistent.p8");

            assertThatThrownBy(brokenUtil::createClientSecret)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Apple Login Error");
        }

        @Test
        @DisplayName("예외 메시지에 '키 파일을 읽을 수 없습니다' 가 포함된다")
        void 예외메시지_키파일_포함() {
            AppleUtil brokenUtil = new AppleUtil();
            ReflectionTestUtils.setField(brokenUtil, "teamId",        TEST_TEAM_ID);
            ReflectionTestUtils.setField(brokenUtil, "clientId",      TEST_CLIENT_ID);
            ReflectionTestUtils.setField(brokenUtil, "keyId",         TEST_KEY_ID);
            ReflectionTestUtils.setField(brokenUtil, "privateKeyPath", "classpath:nonexistent.p8");

            assertThatThrownBy(brokenUtil::createClientSecret)
                    .hasMessageContaining("키 파일을 읽을 수 없습니다");
        }
    }
}