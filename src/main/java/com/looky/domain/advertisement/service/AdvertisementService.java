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
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.OrganizationCategory;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.organization.repository.UniversityRepository;
import com.looky.domain.organization.repository.UserOrganizationRepository;
import com.looky.domain.user.entity.Gender;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.StudentProfileRepository;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final S3Service s3Service;
    private final UniversityRepository universityRepository;
    private final OrganizationRepository organizationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserOrganizationRepository userOrganizationRepository;

    // --- 공개 API ---

    public List<AdvertisementResponse> getActivePopupAdvertisements(User user) {
        UserTarget target = resolveUserTarget(user);
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAscIdAsc(AdvertisementType.POPUP, AdvertisementStatus.ACTIVE)
                .stream()
                .filter(ad -> matchesTarget(ad, target))
                .map(AdvertisementResponse::from)
                .toList();
    }

    public List<AdvertisementResponse> getActiveBannerAdvertisements(User user) {
        UserTarget target = resolveUserTarget(user);
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAscIdAsc(AdvertisementType.BANNER, AdvertisementStatus.ACTIVE)
                .stream()
                .filter(ad -> matchesTarget(ad, target))
                .map(AdvertisementResponse::from)
                .toList();
    }

    public List<AdvertisementResponse> getActiveFloatingAdvertisements(User user) {
        UserTarget target = resolveUserTarget(user);
        return advertisementRepository
                .findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAscIdAsc(AdvertisementType.FLOATING, AdvertisementStatus.ACTIVE)
                .stream()
                .filter(ad -> matchesTarget(ad, target))
                .map(AdvertisementResponse::from)
                .toList();
    }

    // --- 관리자 API ---

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

        List<Long> targetUniversityIds = request.getTargetUniversityIds();
        List<Long> targetOrganizationIds = request.getTargetOrganizationIds();
        validateOrganizationTargets(targetOrganizationIds, targetUniversityIds);

        LocalDateTime now = LocalDateTime.now();
        AdvertisementStatus initialStatus = (!now.isBefore(request.getStartAt()) && !now.isAfter(request.getEndAt()))
                ? AdvertisementStatus.ACTIVE
                : AdvertisementStatus.SCHEDULED;

        Integer displayOrder = null;
        if (initialStatus == AdvertisementStatus.ACTIVE) {
            List<Advertisement> activeAds = advertisementRepository.findActiveByTypeWithLock(request.getAdvertisementType());
            int targetOrder = request.getDisplayOrder() != null
                    ? normalizeTargetOrder(request.getDisplayOrder(), activeAds.size())
                    : activeAds.size();
            shiftOrdersForInsert(activeAds, targetOrder);
            displayOrder = targetOrder;
        }

        Advertisement advertisement = Advertisement.builder()
                .title(request.getTitle())
                .advertisementType(request.getAdvertisementType())
                .imageUrl(request.getImageUrl())
                .landingUrl(request.getLandingUrl())
                .status(initialStatus)
                .displayOrder(displayOrder)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .targetGender(request.getTargetGender())
                .build();

        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);

        applyTargetUniversities(savedAdvertisement, targetUniversityIds);
        applyTargetOrganizations(savedAdvertisement, targetOrganizationIds, targetUniversityIds);

        return savedAdvertisement.getId();
    }

    // 광고 수정
    @Transactional
    public void updateAdvertisement(Long advertisementId, UpdateAdvertisementRequest request) {
        Advertisement advertisement = findAdvertisementById(advertisementId);
        AdvertisementStatus currentStatus = advertisement.getStatus();

        // 상태 변경 처리 (+ 순서 부여/제거)
        AdvertisementStatus newStatus = currentStatus;
        if (request.getStatus().isPresent() && request.getStatus().get() != null) {
            newStatus = request.getStatus().get();
            if (newStatus == AdvertisementStatus.SCHEDULED || newStatus == AdvertisementStatus.ENDED) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "광고 상태는 ACTIVE 또는 INACTIVE만 직접 변경할 수 있습니다.");
            }

            if (currentStatus == AdvertisementStatus.ACTIVE && newStatus == AdvertisementStatus.INACTIVE) {
                // ACTIVE → INACTIVE: 순서 제거 후 나머지 재정렬
                List<Advertisement> activeAds = new ArrayList<>(
                        advertisementRepository.findActiveByTypeWithLock(advertisement.getAdvertisementType())
                );
                advertisement.updateDisplayOrder(null);
                advertisementRepository.flush();
                activeAds.removeIf(ad -> ad.getId().equals(advertisementId));
                reassignSequentialOrders(activeAds);
            } else if (currentStatus != AdvertisementStatus.ACTIVE && newStatus == AdvertisementStatus.ACTIVE) {
                // 비활성 → ACTIVE: 순서 부여
                List<Advertisement> activeAds = advertisementRepository.findActiveByTypeWithLock(advertisement.getAdvertisementType());
                Integer requestedOrder = request.getDisplayOrder().isPresent() ? request.getDisplayOrder().get() : null;
                int targetOrder = requestedOrder != null
                        ? normalizeTargetOrder(requestedOrder, activeAds.size())
                        : activeAds.size();
                shiftOrdersForInsert(activeAds, targetOrder);
                advertisement.updateDisplayOrder(targetOrder);
            }
            advertisement.updateStatus(newStatus);
        }

        // displayOrder 변경 처리 (ACTIVE 유지 시만 적용)
        if (currentStatus == AdvertisementStatus.ACTIVE && newStatus == AdvertisementStatus.ACTIVE
                && request.getDisplayOrder().isPresent() && request.getDisplayOrder().get() != null) {
            int requestedOrder = request.getDisplayOrder().get();
            if (requestedOrder < 0) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "노출 순서는 0 이상이어야 합니다.");
            }
            List<Advertisement> activeAds = advertisementRepository.findActiveByTypeWithLock(advertisement.getAdvertisementType());
            int targetOrder = normalizeTargetOrder(requestedOrder, activeAds.size() - 1);
            reorderAdvertisement(activeAds, advertisement, targetOrder);
        }

        String updatedTitle = request.getTitle().orElse(advertisement.getTitle());
        String updatedImageUrl = request.getImageUrl().orElse(advertisement.getImageUrl());
        String updatedLandingUrl = request.getLandingUrl().orElse(advertisement.getLandingUrl());
        LocalDateTime updatedStartAt = request.getStartAt().orElse(advertisement.getStartAt());
        LocalDateTime updatedEndAt = request.getEndAt().orElse(advertisement.getEndAt());
        Gender updatedTargetGender = request.getTargetGender().isPresent()
                ? request.getTargetGender().get()
                : advertisement.getTargetGender();

        if (updatedEndAt.isBefore(updatedStartAt)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "종료일은 시작일 이후여야 합니다.");
        }

        if (request.getImageUrl().isPresent() && request.getImageUrl().get() != null
                && !request.getImageUrl().get().equals(advertisement.getImageUrl())) {
            s3Service.deleteFile(advertisement.getImageUrl());
        }

        advertisement.update(updatedTitle, updatedImageUrl, updatedLandingUrl, advertisement.getDisplayOrder(), updatedStartAt, updatedEndAt, updatedTargetGender);

        if (request.getTargetUniversityIds().isPresent() || request.getTargetOrganizationIds().isPresent()) {
            List<Long> updatedUniversityIds = request.getTargetUniversityIds().isPresent()
                    ? request.getTargetUniversityIds().get()
                    : advertisement.getTargetUniversities().stream()
                            .map(tu -> tu.getUniversity().getId()).toList();
            List<Long> updatedOrganizationIds = request.getTargetOrganizationIds().isPresent()
                    ? request.getTargetOrganizationIds().get()
                    : advertisement.getTargetOrganizations().stream()
                            .map(to -> to.getOrganization().getId()).toList();

            validateOrganizationTargets(updatedOrganizationIds, updatedUniversityIds);

            advertisement.clearTargetUniversities();
            advertisement.clearTargetOrganizations();
            advertisementRepository.flush();

            applyTargetUniversities(advertisement, updatedUniversityIds);
            applyTargetOrganizations(advertisement, updatedOrganizationIds, updatedUniversityIds);
        }
    }

    // 광고 삭제
    @Transactional
    public void deleteAdvertisement(Long advertisementId) {
        Advertisement advertisement = findAdvertisementById(advertisementId);
        boolean wasActive = advertisement.getStatus() == AdvertisementStatus.ACTIVE;

        List<Advertisement> activeAds = wasActive
                ? new ArrayList<>(advertisementRepository.findActiveByTypeWithLock(advertisement.getAdvertisementType()))
                : null;

        s3Service.deleteFile(advertisement.getImageUrl());
        advertisementRepository.delete(advertisement);
        advertisementRepository.flush();

        if (wasActive) {
            activeAds.removeIf(ad -> ad.getId().equals(advertisementId));
            reassignSequentialOrders(activeAds);
        }
    }

    // --- 스케줄러 ---

    // 스케줄러: SCHEDULED 광고 활성화
    @Transactional
    public void activateScheduledAdvertisements() {
        List<Advertisement> targets = advertisementRepository
                .findAllByStatusAndStartAtLessThanEqual(AdvertisementStatus.SCHEDULED, LocalDateTime.now());
        if (targets.isEmpty()) return;

        Map<AdvertisementType, List<Advertisement>> byType = targets.stream()
                .collect(Collectors.groupingBy(Advertisement::getAdvertisementType));

        for (Map.Entry<AdvertisementType, List<Advertisement>> entry : byType.entrySet()) {
            List<Advertisement> activeAds = advertisementRepository.findActiveByTypeWithLock(entry.getKey());
            int startOrder = activeAds.size();
            List<Advertisement> toActivate = entry.getValue();
            for (int i = 0; i < toActivate.size(); i++) {
                toActivate.get(i).updateDisplayOrder(startOrder + i);
                toActivate.get(i).updateStatus(AdvertisementStatus.ACTIVE);
            }
        }
        log.info("[Scheduler] 광고 활성화 {}건 완료", targets.size());
    }

    // 스케줄러: 만료된 광고 종료 처리
    @Transactional
    public void endExpiredAdvertisements() {
        List<Advertisement> targets = advertisementRepository
                .findAllByStatusAndEndAtLessThan(AdvertisementStatus.ACTIVE, LocalDateTime.now());
        if (targets.isEmpty()) return;

        Set<AdvertisementType> affectedTypes = targets.stream()
                .map(Advertisement::getAdvertisementType)
                .collect(Collectors.toSet());

        targets.forEach(ad -> {
            ad.updateDisplayOrder(null);
            ad.updateStatus(AdvertisementStatus.ENDED);
        });
        advertisementRepository.flush();

        for (AdvertisementType type : affectedTypes) {
            List<Advertisement> remainingActiveAds = advertisementRepository.findActiveByTypeWithLock(type);
            reassignSequentialOrders(remainingActiveAds);
        }
        log.info("[Scheduler] 광고 만료 처리 {}건 완료", targets.size());
    }

    // --- 내부 메서드 ---

    // 광고 ID로 조회 (없으면 예외)
    private Advertisement findAdvertisementById(Long advertisementId) {
        return advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "광고를 찾을 수 없습니다."));
    }

    // 단과대 타겟 지정 시 대학 타겟 필수 및 소속 검증
    private void validateOrganizationTargets(List<Long> organizationIds, List<Long> universityIds) {
        if (organizationIds == null || organizationIds.isEmpty()) return;
        if (universityIds == null || universityIds.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "단과대 타겟을 지정하려면 대학 타겟이 필요합니다.");
        }
        Set<Long> universityIdSet = universityIds.stream().collect(Collectors.toSet());
        for (Long organizationId : organizationIds) {
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "타겟 단과대를 찾을 수 없습니다."));
            if (organization.getCategory() != OrganizationCategory.COLLEGE) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "타겟 조직은 단과대(COLLEGE)여야 합니다.");
            }
            if (!universityIdSet.contains(organization.getUniversity().getId())) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "해당 단과대는 지정된 대학에 속하지 않습니다.");
            }
        }
    }

    // 타겟 대학 목록 적용
    private void applyTargetUniversities(Advertisement advertisement, List<Long> universityIds) {
        if (universityIds == null) return;
        for (Long universityId : universityIds) {
            universityRepository.findById(universityId)
                    .ifPresent(advertisement::addTargetUniversity);
        }
    }

    // 타겟 단과대 목록 적용
    private void applyTargetOrganizations(Advertisement advertisement, List<Long> organizationIds, List<Long> universityIds) {
        if (organizationIds == null) return;
        for (Long organizationId : organizationIds) {
            organizationRepository.findById(organizationId)
                    .ifPresent(advertisement::addTargetOrganization);
        }
    }

    // 유저의 타겟 정보 조회 (대학, 단과대, 성별)
    private UserTarget resolveUserTarget(User user) {
        if (user == null) {
            return new UserTarget(null, null, null);
        }
        Long universityId = studentProfileRepository.findById(user.getId())
                .map(profile -> profile.getUniversity() != null ? profile.getUniversity().getId() : null)
                .orElse(null);
        Long collegeId = userOrganizationRepository
                .findByUserAndOrganizationCategory(user, OrganizationCategory.COLLEGE)
                .stream()
                .findFirst()
                .map(uo -> uo.getOrganization().getId())
                .orElse(null);
        return new UserTarget(universityId, collegeId, user.getGender());
    }

    // 광고 타겟과 유저 정보 매칭 여부 확인
    private boolean matchesTarget(Advertisement ad, UserTarget target) {
        if (!ad.getTargetUniversities().isEmpty()) {
            if (target.universityId() == null) return false;
            boolean universityMatches = ad.getTargetUniversities().stream()
                    .anyMatch(tu -> tu.getUniversity().getId().equals(target.universityId()));
            if (!universityMatches) return false;
        }
        if (!ad.getTargetOrganizations().isEmpty()) {
            if (target.collegeId() == null) return false;
            boolean organizationMatches = ad.getTargetOrganizations().stream()
                    .anyMatch(to -> to.getOrganization().getId().equals(target.collegeId()));
            if (!organizationMatches) return false;
        }
        if (ad.getTargetGender() != null) {
            return target.gender() != null && ad.getTargetGender().equals(target.gender());
        }
        return true;
    }

    // 요청 순서를 유효 범위로 보정
    private int normalizeTargetOrder(int requestedOrder, int maxOrder) {
        if (maxOrder <= 0) {
            return 0;
        }
        return Math.min(requestedOrder, maxOrder);
    }

    // 삽입 위치 확보를 위해 기존 순서 밀기
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

    // 특정 광고를 목표 순서로 이동 후 전체 재정렬
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

    // 0부터 순차적으로 노출 순서 재할당
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

    // unique 제약 충돌 방지를 위해 순서를 임시로 음수로 변경
    private void temporarilyMoveOrders(List<Advertisement> advertisements) {
        for (int index = 0; index < advertisements.size(); index++) {
            advertisements.get(index).updateDisplayOrder(-(index + 1));
        }
        advertisementRepository.flush();
    }

    private record UserTarget(Long universityId, Long collegeId, Gender gender) {}
}
