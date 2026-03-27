package com.looky.domain.advertisement.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.response.PageResponse;
import com.looky.common.service.S3Service;
import com.looky.domain.advertisement.dto.AdminAdvertisementResponse;
import com.looky.domain.advertisement.dto.AdvertisementResponse;
import com.looky.domain.advertisement.dto.CreateAdvertisementRequest;
import com.looky.domain.advertisement.dto.UpdateAdvertisementRequest;
import com.looky.domain.advertisement.entity.Advertisement;
import com.looky.domain.advertisement.entity.AdvertisementStatus;
import com.looky.domain.advertisement.entity.AdvertisementType;
import com.looky.domain.advertisement.repository.AdvertisementRepository;
import com.looky.domain.advertisement.repository.AdvertisementSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final S3Service s3Service;

    // 팝업 광고 조회
    public List<AdvertisementResponse> getActivePopupAdvertisements() {
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAscIdAsc(AdvertisementType.POPUP, AdvertisementStatus.ACTIVE)
                .stream()
                .map(AdvertisementResponse::from)
                .toList();
    }

    // 배너 광고 조회
    public List<AdvertisementResponse> getActiveBannerAdvertisements() {
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAscIdAsc(AdvertisementType.BANNER, AdvertisementStatus.ACTIVE)
                .stream()
                .map(AdvertisementResponse::from)
                .toList();
    }

    // 플로팅 광고 조회
    public List<AdvertisementResponse> getActiveFloatingAdvertisements() {
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAscIdAsc(AdvertisementType.FLOATING, AdvertisementStatus.ACTIVE)
                .stream()
                .map(AdvertisementResponse::from)
                .toList();
    }

    // 전체 광고 조회 (관리자용)
    public PageResponse<AdminAdvertisementResponse> getAdvertisements(AdvertisementType type, AdvertisementStatus status, Pageable pageable) {
        Specification<Advertisement> spec = Specification
                .where(AdvertisementSpecification.hasType(type))
                .and(AdvertisementSpecification.hasStatus(status));

        Page<AdminAdvertisementResponse> page = advertisementRepository.findAll(spec, pageable)
                .map(AdminAdvertisementResponse::from);
        return PageResponse.from(page);
    }

    // 광고 생성
    @Transactional
    public Long createAdvertisement(CreateAdvertisementRequest request) {
        if (request.getEndAt().isBefore(request.getStartAt())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "종료일은 시작일 이후여야 합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        AdvertisementStatus initialStatus = (!now.isBefore(request.getStartAt()) && !now.isAfter(request.getEndAt()))
                ? AdvertisementStatus.ACTIVE
                : AdvertisementStatus.SCHEDULED;

        List<Advertisement> advertisements = advertisementRepository.findAllByAdvertisementTypeOrderByDisplayOrderAscIdAsc(request.getAdvertisementType());
        int targetOrder = normalizeTargetOrder(request.getDisplayOrder(), advertisements.size());

        shiftOrdersForInsert(advertisements, targetOrder);

        Advertisement advertisement = Advertisement.builder()
                .title(request.getTitle())
                .advertisementType(request.getAdvertisementType())
                .imageUrl(request.getImageUrl())
                .landingUrl(request.getLandingUrl())
                .status(initialStatus)
                .displayOrder(targetOrder)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();

        return advertisementRepository.save(advertisement).getId();
    }

    // 광고 수정
    @Transactional
    public void updateAdvertisement(Long advertisementId, UpdateAdvertisementRequest request) {
        Advertisement advertisement = findAdvertisementById(advertisementId);

        if (request.getStatus().isPresent() && request.getStatus().get() != null) {
            AdvertisementStatus newStatus = request.getStatus().get();
            if (newStatus == AdvertisementStatus.SCHEDULED || newStatus == AdvertisementStatus.ENDED) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "광고 상태는 ACTIVE 또는 INACTIVE만 직접 변경할 수 있습니다.");
            }
            advertisement.updateStatus(newStatus);
        }

        if (request.getDisplayOrder().isPresent()) {
            Integer displayOrder = request.getDisplayOrder().get();
            if (displayOrder == null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "노출 순서는 필수입니다.");
            }
            if (displayOrder < 0) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "노출 순서는 0 이상이어야 합니다.");
            }
        }

        String updatedTitle = request.getTitle().orElse(advertisement.getTitle());
        String updatedImageUrl = request.getImageUrl().orElse(advertisement.getImageUrl());
        String updatedLandingUrl = request.getLandingUrl().orElse(advertisement.getLandingUrl());
        Integer updatedDisplayOrder = request.getDisplayOrder().orElse(advertisement.getDisplayOrder());
        LocalDateTime updatedStartAt = request.getStartAt().orElse(advertisement.getStartAt());
        LocalDateTime updatedEndAt = request.getEndAt().orElse(advertisement.getEndAt());

        if (updatedEndAt.isBefore(updatedStartAt)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "종료일은 시작일 이후여야 합니다.");
        }

        if (request.getImageUrl().isPresent() && request.getImageUrl().get() != null
                && !request.getImageUrl().get().equals(advertisement.getImageUrl())) {
            s3Service.deleteFile(advertisement.getImageUrl());
        }

        if (request.getDisplayOrder().isPresent()) {
            List<Advertisement> advertisements = advertisementRepository
                    .findAllByAdvertisementTypeOrderByDisplayOrderAscIdAsc(advertisement.getAdvertisementType());
            int targetOrder = normalizeTargetOrder(updatedDisplayOrder, advertisements.size() - 1);
            reorderAdvertisement(advertisements, advertisement, targetOrder);
            updatedDisplayOrder = targetOrder;
        }

        advertisement.update(
                updatedTitle,
                updatedImageUrl,
                updatedLandingUrl,
                updatedDisplayOrder,
                updatedStartAt,
                updatedEndAt
        );
    }

    // 광고 삭제
    @Transactional
    public void deleteAdvertisement(Long advertisementId) {
        Advertisement advertisement = findAdvertisementById(advertisementId);
        List<Advertisement> advertisements = new ArrayList<>(
                advertisementRepository.findAllByAdvertisementTypeOrderByDisplayOrderAscIdAsc(advertisement.getAdvertisementType())
        );

        s3Service.deleteFile(advertisement.getImageUrl());
        advertisementRepository.delete(advertisement);
        advertisementRepository.flush();

        advertisements.removeIf(target -> target.getId().equals(advertisementId));
        reassignSequentialOrders(advertisements);
    }

    @Transactional
    public void activateScheduledAdvertisements() {
        List<Advertisement> targets = advertisementRepository
                .findAllByStatusAndStartAtLessThanEqual(AdvertisementStatus.SCHEDULED, LocalDateTime.now());
        targets.forEach(advertisement -> advertisement.updateStatus(AdvertisementStatus.ACTIVE));
        log.info("[Scheduler] 광고 활성화 {}건 완료", targets.size());
    }

    @Transactional
    public void endExpiredAdvertisements() {
        List<Advertisement> targets = advertisementRepository
                .findAllByStatusAndEndAtLessThan(AdvertisementStatus.ACTIVE, LocalDateTime.now());
        targets.forEach(advertisement -> advertisement.updateStatus(AdvertisementStatus.ENDED));
        log.info("[Scheduler] 광고 만료 처리 {}건 완료", targets.size());
    }

    private Advertisement findAdvertisementById(Long advertisementId) {
        return advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "광고를 찾을 수 없습니다."));
    }

    private int normalizeTargetOrder(int requestedOrder, int maxOrder) {
        if (maxOrder <= 0) {
            return 0;
        }
        return Math.min(requestedOrder, maxOrder);
    }

    private void shiftOrdersForInsert(List<Advertisement> advertisements, int targetOrder) {
        if (advertisements.isEmpty()) {
            return;
        }

        temporarilyMoveOrders(advertisements);

        for (int index = 0; index < advertisements.size(); index++) {
            int displayOrder = index < targetOrder ? index : index + 1;
            advertisements.get(index).updateDisplayOrder(displayOrder);
        }
        advertisementRepository.flush();
    }

    private void reorderAdvertisement(
            List<Advertisement> advertisements,
            Advertisement targetAdvertisement,
            int targetOrder
    ) {
        List<Advertisement> orderedAdvertisements = new ArrayList<>(advertisements);
        orderedAdvertisements.removeIf(advertisement -> advertisement.getId().equals(targetAdvertisement.getId()));
        orderedAdvertisements.add(targetOrder, targetAdvertisement);
        reassignSequentialOrders(orderedAdvertisements);
    }

    private void reassignSequentialOrders(List<Advertisement> advertisements) {
        if (advertisements.isEmpty()) {
            return;
        }

        temporarilyMoveOrders(advertisements);

        for (int index = 0; index < advertisements.size(); index++) {
            advertisements.get(index).updateDisplayOrder(index);
        }
        advertisementRepository.flush();
    }

    private void temporarilyMoveOrders(List<Advertisement> advertisements) {
        for (int index = 0; index < advertisements.size(); index++) {
            advertisements.get(index).updateDisplayOrder(-(index + 1));
        }
        advertisementRepository.flush();
    }
}
