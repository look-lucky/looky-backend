package com.looky.domain.admin.service;

import com.looky.domain.admin.dto.RefreshTokenResponse;
import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthManageService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "RT:";

    // 특정 유저 리프레시 토큰 조회
    public String getToken(Long userId) {

        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    // 특정 유저 리프레시 토큰 삭제
    public void deleteToken(Long userId) {
        String key = PREFIX + userId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 사용자의 토큰을 찾을 수 없습니다.");
        }
        redisTemplate.delete(key);
    }

    // 모든 리프레시 토큰 조회
    public List<RefreshTokenResponse> getAllTokens() {
        List<RefreshTokenResponse> tokens = new ArrayList<>();

        // keys 명령어 대신 scan을 사용하여 부하 분산
        ScanOptions options = ScanOptions.scanOptions().match(PREFIX + "*").count(100).build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String value = redisTemplate.opsForValue().get(key);
                Long ttl = redisTemplate.getExpire(key); // 남은 시간 조회

                // 키에서 userId 추출 (RT:1 -> 1)
                Long userId = Long.parseLong(key.substring(PREFIX.length()));

                tokens.add(RefreshTokenResponse.builder()
                        .userId(userId)
                        .refreshToken(value)
                        .timeToLive(ttl)
                        .build());
            }
        }

        return tokens;
    }
}