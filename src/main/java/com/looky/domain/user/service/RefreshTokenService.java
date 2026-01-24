package com.looky.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "RT:";

    public void save(Long userId, String refreshToken) {
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, 14, TimeUnit.DAYS);
    }

    public String getByUserId(Long userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    public void delete(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
