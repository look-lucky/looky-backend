package com.looky.domain.store.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.S3Service;
import com.looky.domain.store.dto.BizVerificationRequest;
import com.looky.domain.store.dto.BizVerificationResponse;
import com.looky.domain.store.dto.StoreClaimRequest;
import com.looky.domain.store.dto.MyStoreClaimResponse;
import com.looky.domain.store.entity.StoreClaim;
import com.looky.domain.store.entity.StoreClaimStatus;
import com.looky.domain.store.repository.StoreClaimRepository;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.store.dto.StoreResponse;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.looky.domain.store.dto.BizValidationApiRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreClaimService {

    private final S3Service s3Service;
    private final StoreClaimRepository  storeClaimRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Value("${open-api.service-key}")
    private String serviceKey;
    private static final String API_URL = "https://api.odcloud.kr/api/nts-businessman/v1/validate"; // 사업자등록정보 진위확인 API

    // 미등록된 상점 조회
    public List<StoreResponse> searchUnclaimedStores(String keyword) {
        log.debug("[StoreClaimService] searchUnclaimedStores 호출 - keyword: '{}'", keyword);
        List<com.looky.domain.store.entity.Store> stores = storeRepository.findUnclaimedByNameOrAddress(keyword);
        log.debug("[StoreClaimService] DB 조회 결과 - 총 {}건", stores.size());
        stores.forEach(store -> log.debug("[StoreClaimService] 상점 - id: {}, name: {}, roadAddress: {}, storeStatus: {}",
                store.getId(), store.getName(), store.getRoadAddress(), store.getStoreStatus()));
        return stores.stream()
                .map(StoreResponse::from)
                .collect(Collectors.toList());
    }

    // 사업자등록번호 유효성 검증
    public BizVerificationResponse verifyBizRegNo(BizVerificationRequest request) {
        log.info("사업자등록정보 진위확인 요청: {}", request);

        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromUriString(API_URL + "?serviceKey=" + serviceKey)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 데이터 정제 (하이픈 제거) 및 외부 API 요청 DTO 변환
        if (request.getBizs() == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "사업자 정보 목록이 비어있습니다.");
        }
        
        List<BizValidationApiRequest.BusinessInfo> businessInfos = request.getBizs().stream()
                .map(biz -> BizValidationApiRequest.BusinessInfo.builder()
                        .bNo(biz.getBNo() != null ? biz.getBNo().replaceAll("-", "") : null)
                        .startDt(biz.getStartDt() != null ? biz.getStartDt().replaceAll("-", "") : null)
                        .pNm(biz.getPNm())
                        .build())
                .collect(Collectors.toList());

        BizValidationApiRequest apiRequest = BizValidationApiRequest.builder()
                .businesses(businessInfos)
                .build();

        HttpEntity<BizValidationApiRequest> entity = new HttpEntity<>(apiRequest, headers);

        try {
            ResponseEntity<BizVerificationResponse> responseEntity = restTemplate.postForEntity(uri, entity,
                    BizVerificationResponse.class);

            BizVerificationResponse response = responseEntity.getBody();

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {

                BizVerificationResponse.BizStatus status = response.getData().get(0);

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
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("사업자등록정보 진위 확인 중 오류 발생", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "사업자등록정보 진위확인 중 오류가 발생했습니다.");
        }
    }

    // 상점 소유권 신청   
    public Long createStoreClaims(User user, StoreClaimRequest request, MultipartFile image) throws IOException {

        User owner = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (owner.getRole() != Role.ROLE_OWNER) {
             throw new CustomException(ErrorCode.FORBIDDEN, "점주만 가게 점유 신청을 할 수 있습니다.");
        }

        storeRepository.findById(request.getStoreId()).orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 상점입니다."));


        if (storeClaimRepository.existsByStoreIdAndStatus(request.getStoreId(), StoreClaimStatus.PENDING)) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "이미 같은 가게에 대해 승인 대기 중인 요청이 존재합니다.");
        }

        // 이미지 S3에 업로드
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.uploadFile(image);
        }

        StoreClaim storeClaim = request.toEntity(imageUrl);
        StoreClaim savedStoreClaim = storeClaimRepository.save(storeClaim);

        return savedStoreClaim.getId();
    }

    public List<MyStoreClaimResponse> getMyStoreClaims(User user) {
        return storeClaimRepository.findByUserId(user.getId()).stream()
                .map(MyStoreClaimResponse::from)
                .collect(Collectors.toList());
    }
}
