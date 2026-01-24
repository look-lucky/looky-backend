package com.looky.security.jwt;

import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import com.looky.security.details.PrincipalDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    @Getter
    private final long accessTokenExpiresIn;
    @Getter
    private final long refreshTokenExpiresIn;

    private static final String KEY_ROLE = "role";
    private static final String KEY_TYPE = "type";

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessTokenExpiresIn,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpiresIn) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    // Access Token 생성
    public String createAccessToken(Long userId, String username, String role) {
        return createToken(userId, username, role, "access", accessTokenExpiresIn);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long userId, String username, String role) {
        return createToken(userId, username, role, "refresh", refreshTokenExpiresIn);
    }

    private String createToken(Long userId, String username, String role, String type, long expiresIn) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim(KEY_ROLE, role)
                .claim(KEY_TYPE, type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiresIn))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰에서 인증 정보 조회
    // DB 조회 없이 토큰 내의 정보만으로 UserDetails 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (claims == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        String userId = claims.getSubject();
        String username = claims.get("username", String.class);
        String role = claims.get(KEY_ROLE, String.class);

        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(role));

        // 토큰 정보로 가짜 사용자 객체 생성
        User user = User.builder()
                .username(username)
                .role(Role.valueOf(role))
                .password("")
                .build();

        user.setUserId(Long.valueOf(userId));

        // 일반/소셜 공통 사용자 인증 객체 생성
        PrincipalDetails principal = new PrincipalDetails(user);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // Claims 파싱 (내부 및 외부 서비스용)
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료되어도 정보는 필요할 수 있음
        } catch (Exception e) {
            return null;
        }
    }

    // 토큰에서 UserId 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        if (claims == null)
            throw new RuntimeException("토큰 파싱 실패");
        return Long.valueOf(claims.getSubject());
    }
}