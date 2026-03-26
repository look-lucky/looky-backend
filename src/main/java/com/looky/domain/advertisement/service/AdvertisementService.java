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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final S3Service s3Service;

    public Optional<AdvertisementResponse> getActivePopupAdvertisement() {
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAsc(AdvertisementType.POPUP, AdvertisementStatus.ACTIVE)
                .stream()
                .findFirst()
                .map(AdvertisementResponse::from);
    }

    public List<AdvertisementResponse> getActiveBannerAdvertisements() {
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAsc(AdvertisementType.BANNER, AdvertisementStatus.ACTIVE)
                .stream()
                .map(AdvertisementResponse::from)
                .toList();
    }

    public List<AdvertisementResponse> getActiveFloatingAdvertisements() {
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAsc(AdvertisementType.FLOATING, AdvertisementStatus.ACTIVE)
                .stream()
                .map(AdvertisementResponse::from)
                .toList();
    }

    public PageResponse<AdminAdvertisementResponse> getAdvertisements(AdvertisementType type, AdvertisementStatus status, Pageable pageable) {
        Specification<Advertisement> spec = Specification
                .where(AdvertisementSpecification.hasType(type))
                .and(AdvertisementSpecification.hasStatus(status));

        Page<AdminAdvertisementResponse> page = advertisementRepository.findAll(spec, pageable)
                .map(AdminAdvertisementResponse::from);
        return PageResponse.from(page);
    }

    @Transactional
    public Long createAdvertisement(CreateAdvertisementRequest request) {
        if (request.getEndAt().isBefore(request.getStartAt())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "종료일은 시작일 이후여야 합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        AdvertisementStatus initialStatus = (!now.isBefore(request.getStartAt()) && !now.isAfter(request.getEndAt()))
                ? AdvertisementStatus.ACTIVE
                : AdvertisementStatus.SCHEDULED;

        Advertisement advertisement = Advertisement.builder()
                .title(request.getTitle())
                .advertisementType(request.getAdvertisementType())
                .imageUrl(request.getImageUrl())
                .landingUrl(request.getLandingUrl())
                .status(initialStatus)
                .displayOrder(request.getDisplayOrder())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();

        return advertisementRepository.save(advertisement).getId();
    }

    @Transactional
    public void updateAdvertisement(Long advertisementId, UpdateAdvertisementRequest request) {
        Advertisement advertisement = findAdvertisementById(advertisementId);

        if (request.getStatus().isPresent() && request.getStatus().get() != null) {
            AdvertisementStatus newStatus = request.getStatus().get();
            if (newStatus == AdvertisementStatus.SCHEDULED || newStatus == AdvertisementStatus.ENDED) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "해당 상태로 변경할 수 없습니다.");
            }
            advertisement.updateStatus(newStatus);
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

        advertisement.update(updatedTitle, updatedImageUrl, updatedLandingUrl, updatedDisplayOrder, updatedStartAt, updatedEndAt);
    }

    @Transactional
    public void deleteAdvertisement(Long advertisementId) {
        Advertisement advertisement = findAdvertisementById(advertisementId);
        s3Service.deleteFile(advertisement.getImageUrl());
        advertisementRepository.delete(advertisement);
    }

    // 스케줄러에서 호출
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
}
