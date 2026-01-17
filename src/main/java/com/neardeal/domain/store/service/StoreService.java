package com.neardeal.domain.store.service;

import com.neardeal.common.service.S3Service;
import com.neardeal.domain.store.entity.StoreCategory;

import com.neardeal.common.exception.CustomException;
import com.neardeal.common.exception.ErrorCode;
import com.neardeal.common.response.PageResponse;
import com.neardeal.domain.store.dto.*;
import com.neardeal.domain.store.entity.Store;
import com.neardeal.domain.store.entity.StoreImage;
import com.neardeal.domain.store.entity.StoreReport;
import com.neardeal.domain.store.entity.StoreReportReason;
import java.util.HashSet;
import java.util.Set;
import com.neardeal.domain.store.repository.StoreReportRepository;
import com.neardeal.domain.store.repository.StoreRepository;
import com.neardeal.domain.user.entity.Role;
import com.neardeal.domain.user.repository.UserRepository;
import com.neardeal.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreReportRepository storeReportRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createStore(User user, CreateStoreRequest request, List<MultipartFile> images) throws IOException {

        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (owner.getRole() != Role.ROLE_OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (storeRepository.existsByName(request.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 상점 이름입니다.");
        }

        Store store = request.toEntity(owner);

        // 이미지 S3 업로드 및 리스트 순서대로 DB 저장
        uploadAndSaveImages(store, images);

        Store savedStore = storeRepository.save(store);

        return savedStore.getId();
    }

    public StoreResponse getStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상점을 찾을 수 없습니다."));

        return StoreResponse.from(store);
    }

    public PageResponse<StoreResponse> getStores(String keyword, StoreCategory category, Pageable pageable) {
        Page<Store> storePage;

        if (keyword != null && category != null) {
            storePage = storeRepository.findByNameContainingAndStoreCategoriesContains(keyword, category, pageable);
        } else if (keyword != null) {
            storePage = storeRepository.findByNameContaining(keyword, pageable);
        } else if (category != null) {
            storePage = storeRepository.findByStoreCategoriesContains(category, pageable);
        } else {
            storePage = storeRepository.findAll(pageable);
        }

        Page<StoreResponse> responsePage = storePage.map(StoreResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional
    public void updateStore(Long storeId, User user, UpdateStoreRequest request, List<MultipartFile> images)
            throws IOException {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        // 본인 소유 확인
        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            log.warn("[UpdateStore] Forbidden attempt. storeId={}, requesterUserId={}", storeId, owner.getId());
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
        }

        if (!store.getName().equals(request.getName()) && storeRepository.existsByName(request.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 상점 이름입니다.");
        }

        store.updateStore(
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getPhoneNumber(),
                request.getIntroduction(),
                request.getOperatingHours(),
                request.getStoreCategories() != null ? new HashSet<>(request.getStoreCategories()) : null,
                request.getStoreMoods() != null ? new HashSet<>(request.getStoreMoods()) : null);

        // 새 이미지가 존재하면 기존 것 모두 삭제 후 새로 등록
        if (images != null && !images.isEmpty()) {

            // S3 파일 삭제
            for (StoreImage oldImage : store.getImages()) {
                s3Service.deleteFile(oldImage.getImageUrl());
            }

            // DB 삭제
            store.getImages().clear();

            // 새 이미지 업로드
            uploadAndSaveImages(store, images);
        }
    }

    // 상점 이미지 개별 삭제
    @Transactional
    public void deleteStoreImage(Long storeId, Long imageId, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        // 본인 소유 확인
        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
        }

        // 삭제할 이미지 찾기
        StoreImage targetImage = store.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 이미지가 존재하지 않습니다."));

        // S3 삭제
        s3Service.deleteFile(targetImage.getImageUrl());

        // DB 삭제
        store.removeImage(targetImage);
    }

    // 상점 삭제
    @Transactional
    public void deleteStore(Long storeId, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        // 본인 소유 확인
        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
        }

        storeRepository.delete(store);
    }

    public List<StoreResponse> getMyStores(User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Store> stores = storeRepository.findAllByUser(owner);
        return stores.stream().map(StoreResponse::from).toList();
    }

    // S3에 업로드 및 DB 저장
    private void uploadAndSaveImages(Store store, List<MultipartFile> images) throws IOException {

        if (images == null || images.isEmpty()) {
            return;
        }

        // 기존 이미지 개수 파악하여 인덱스 시작점 설정
        int currentOrderIndex = store.getImages().size();

        for (MultipartFile file : images) {

            if (file.isEmpty())
                continue;

            // S3에 저장
            String imageUrl = s3Service.uploadFile(file);

            // DB에 저장
            StoreImage storeImage = StoreImage.builder()
                    .store(store)
                    .imageUrl(imageUrl)
                    .orderIndex(currentOrderIndex++) // 인덱스 1씩 증가 시키며 저장
                    .build();
            store.addImage(storeImage);
        }
    }

    // 상점 신고
    @Transactional
    public void reportStore(Long storeId, Long reporterId, StoreReportRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        User reporter = userRepository.getReferenceById(reporterId);

        if (storeReportRepository.existsByStoreAndReporter(store, reporter)) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "이미 신고한 상점입니다.");
        }

        // List -> Set 변환
        Set<StoreReportReason> reasons = new HashSet<>(request.getReasons());

        if (reasons.contains(StoreReportReason.ETC)) {
            if (!StringUtils.hasText(request.getDetail())) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "기타 사유 선택 시 상세 내용은 필수입니다.");
            }
        }

        StoreReport report = StoreReport.builder()
                .store(store)
                .reporter(reporter)
                .reasons(reasons)
                .detail(request.getDetail())
                .build();

        storeReportRepository.save(report);
    }
}
