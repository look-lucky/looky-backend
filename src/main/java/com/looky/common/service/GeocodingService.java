package com.looky.common.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodingService {

    private final StoreRepository storeRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${naver.maps.client-id}")
    private String clientId;

    @Value("${naver.maps.client-secret}")
    private String clientSecret;

    private static final String NAVER_GEOCODE_URL = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode";

    public static class Coordinate {
        public final Double latitude;
        public final Double longitude;
        
        public Coordinate(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public Coordinate getCoordinate(String roadAddress) {
        if (roadAddress == null || roadAddress.trim().isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "주소가 비어있습니다.");
        }

        // 주소 전처리 (특수 공백 제거 등)
        String query = refineAddress(roadAddress);

        // 요청 URL 생성 (URLEncoder 사용)
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI uri = URI.create(NAVER_GEOCODE_URL + "?query=" + encodedQuery);

        log.info("[Geocoding] Generated URI: {}", uri);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId != null ? clientId.trim() : "");
        headers.set("X-NCP-APIGW-API-KEY", clientSecret != null ? clientSecret.trim() : "");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();

        if (body == null || !body.containsKey("addresses")) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "지오코딩 API 응답 오류");
        }

        List<Map<String, Object>> addresses = (List<Map<String, Object>>) body.get("addresses");

        if (addresses.isEmpty()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "지오코딩 좌표 검색 결과 없음");
        }

        Map<String, Object> firstResult = addresses.get(0);
        Double latitude = Double.valueOf((String) firstResult.get("y"));
        Double longitude = Double.valueOf((String) firstResult.get("x"));

        return new Coordinate(latitude, longitude);
    }

    //  도로명 주소 기반으로 위도 / 경도 업데이트
    @Async
    @Transactional
    public void updateLocation(Long storeId, String roadAddress) {
        try {
            Coordinate coordinate = getCoordinate(roadAddress);
            
            // DB 업데이트
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                store.updateLocation(coordinate.latitude, coordinate.longitude);
                log.info("[Geocoding] Updated location for storeId={}: lat={}, lng={}", storeId, coordinate.latitude, coordinate.longitude);
            }
        } catch (CustomException e) {
            log.warn("[Geocoding] Failed for storeId={}: {}", storeId, e.getMessage());
            markAsNeedCheck(storeId, "지오코딩 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("[Geocoding] Error processing storeId={}", storeId, e);
            markAsNeedCheck(storeId, "지오코딩 오류: " + e.getMessage());
        }
    }

    private void markAsNeedCheck(Long storeId, String reason) {
        storeRepository.findById(storeId).ifPresent(store -> {
            store.markAsNeedCheck(reason);
            log.warn("[Geocoding] Store marked for manual check: {} (Reason: {})", storeId, reason);
        });
    }

    private String refineAddress(String address) {
        if (address == null) return "";

        //  한글, 영문, 숫자, 공백, 소괄호, 대시, 쉼표, 점 외에는 모두 제거 (공백으로 치환)
        String refined = address.replaceAll("[^0-9a-zA-Z가-힣\\s\\(\\)\\-,.]", " ");
        
        // 다중 공백을 단일 공백으로 정리
        refined = refined.replaceAll("\\s+", " ").trim();

        return refined;
    }
}
