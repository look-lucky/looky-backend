package com.looky.domain.store.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.store.dto.BusinessVerificationRequest;
import com.looky.domain.store.dto.BusinessVerificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class StoreVerificationService {

    @Value("${open-api.service-key}")
    private String serviceKey;

    private static final String API_URL = "https://api.odcloud.kr/api/nts-businessman/v1/validate";

    public BusinessVerificationResponse verifyBusiness(BusinessVerificationRequest request) {

        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromUriString(API_URL + "?serviceKey=" + serviceKey)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<BusinessVerificationRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<BusinessVerificationResponse> responseEntity = restTemplate.postForEntity(uri, entity,
                    BusinessVerificationResponse.class);

            BusinessVerificationResponse response = responseEntity.getBody();

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {

                BusinessVerificationResponse.BusinessStatus status = response.getData().get(0);

                // 국세청 등록 정보 일치 여부 확인
                if (!"01".equals(status.getValid())) {
                    throw new CustomException(ErrorCode.VALIDATION_FAILED, "사업자 정보가 국세청 등록 정보와 일치하지 않습니다.");
                }

                // 사업 상태 확인 (계속사업자 -> 01)
                if (status.getStatus() != null) {
                    String bSttCd = status.getStatus().getBSttCd();
                    if (!"01".equals(bSttCd)) {
                        throw new CustomException(ErrorCode.VALIDATION_FAILED, "휴업 또는 폐업 상태의 사업자는 등록할 수 없습니다.");
                    }
                }
            }

            return response;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "사업자등록정보 진위확인 중 오류가 발생했습니다.");
        }
    }
}
