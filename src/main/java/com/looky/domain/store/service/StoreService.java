package com.looky.domain.store.service;

import com.looky.common.service.S3Service;
import com.looky.domain.coupon.entity.CouponUsageStatus;
import com.looky.domain.store.entity.*;
import com.looky.domain.store.repository.StoreSpecification;
import org.springframework.data.jpa.domain.Specification;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.response.PageResponse;
import com.looky.domain.store.dto.*;
import com.looky.domain.partnership.dto.PartnershipInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.looky.domain.store.repository.StoreReportRepository;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.repository.UserRepository;
import com.looky.domain.item.repository.ItemRepository;
import com.looky.domain.user.entity.User;
import com.looky.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.looky.domain.favorite.repository.FavoriteRepository;
import com.looky.domain.user.entity.StudentProfile;
import com.looky.domain.user.repository.StudentProfileRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import com.looky.domain.partnership.repository.PartnershipRepository;
import com.looky.domain.partnership.service.PartnershipService;
import com.looky.domain.coupon.repository.CouponRepository;
import com.looky.domain.partnership.entity.Partnership;
import com.looky.domain.coupon.entity.Coupon;
import java.util.stream.Collectors;
import com.looky.domain.coupon.repository.StudentCouponRepository;
import com.looky.domain.organization.repository.UniversityRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreReportRepository storeReportRepository;
    private final ItemRepository itemRepository;
    private final S3Service s3Service;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PartnershipRepository partnershipRepository;
    private final PartnershipService partnershipService;
    private final CouponRepository couponRepository;
    private final StudentCouponRepository studentCouponRepository;
    private final UniversityRepository universityRepository;

    // --- 점주용 ---

    // 상점 등록
    @Transactional
    public Long createStoreForOwner(User user, StoreCreateRequest request) {

        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (owner.getRole() != Role.ROLE_OWNER && owner.getRole() != Role.ROLE_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN, "점주 회원 또는 관리자만 가게를 등록할 수 있습니다.");
        }

        if (storeRepository.existsByNameAndNormalizedBranch(request.getName(), normalizeBranch(request.getBranch()))) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 상점 이름/지점명 조합입니다.");
        }

        if (StringUtils.hasText(request.getBizRegNo()) && storeRepository.existsByBizRegNo(request.getBizRegNo())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 사업자등록번호입니다.");
        }

        List<String> imageUrls = request.getImageUrls();
        validateImageLimit(imageUrls, 3, "일반 이미지는 최대 3장까지 등록할 수 있습니다.");

        List<String> menuBoardImageUrls = request.getMenuBoardImageUrls();
        validateImageLimit(menuBoardImageUrls, 10, "메뉴판 이미지는 최대 10장까지 등록할 수 있습니다.");

        Store store = request.toEntity(owner);

        if (request.getProfileImageUrl() != null) {
            store.updateStore(
                    store.getName(), store.getBranch(), store.getRoadAddress(), store.getJibunAddress(),
                    store.getLatitude(), store.getLongitude(), store.getStorePhone(), store.getIntroduction(),
                    store.getOperatingHours(), store.getStoreCategories(), store.getStoreMoods(),
                    store.getHolidayDates(), store.getIsSuspended(), store.getRepresentativeName(),
                    request.getProfileImageUrl()
            );
        }

        addStoreImages(store, imageUrls);
        addMenuBoardImages(store, menuBoardImageUrls);

        Store savedStore = storeRepository.save(store);

        if (request.getUniversityIds() != null) {
            for (Long universityId : request.getUniversityIds()) {
                universityRepository.findById(universityId)
                        .ifPresent(savedStore::addUniversity);
            }
        }

        recalculateCloverGrade(savedStore);

        return savedStore.getId();
    }

    // 상점 정보 수정
    @Transactional
    public void updateStoreForOwner(Long storeId, User user, StoreUpdateRequest request) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        if (store.getStoreStatus() == StoreStatus.ACTIVE) {
            if (owner.getRole() == Role.ROLE_ADMIN) {
                throw new CustomException(ErrorCode.FORBIDDEN, "점유된 상점은 관리자가 수정할 수 없습니다.");
            }
            if (!Objects.equals(store.getUser().getId(), owner.getId())) {
                log.warn("[UpdateStore] Forbidden attempt. storeId={}, requesterUserId={}", storeId, owner.getId());
                throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
            }
        } else {
            if (owner.getRole() != Role.ROLE_ADMIN) {
                throw new CustomException(ErrorCode.FORBIDDEN, "해당 상점은 관리자만 수정할 수 있습니다.");
            }
        }

        // 이미 존재하는 상점인지 판단 (상점명 + 지점명 기준)
        String updatedName = request.getName().orElse(store.getName());
        String updatedBranch = request.getBranch().orElse(store.getBranch());
        boolean storeIdentityChanged = !Objects.equals(store.getName(), updatedName) || !Objects.equals(normalizeBranch(store.getBranch()), normalizeBranch(updatedBranch));

        if (storeIdentityChanged && storeRepository.existsByNameAndNormalizedBranchAndIdNot(updatedName, normalizeBranch(updatedBranch), storeId)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 상점 이름/지점명 조합입니다.");
        }

        // 프로필 이미지 처리
        String profileImageUrl = store.getProfileImageUrl();
        if (request.getProfileImageUrl().isPresent()) {
            String newProfileImageUrl = request.getProfileImageUrl().get();
            if (!Objects.equals(profileImageUrl, newProfileImageUrl)) {
                if (profileImageUrl != null) {
                    s3Service.deleteFile(profileImageUrl);
                }
                profileImageUrl = newProfileImageUrl; // null(삭제) 또는 새 URL(교체)
            }
        }

        store.updateStore(
                request.getName().orElse(store.getName()),
                sanitizeBranch(updatedBranch),
                request.getRoadAddress().orElse(store.getRoadAddress()),
                request.getJibunAddress().orElse(store.getJibunAddress()),
                request.getLatitude().orElse(store.getLatitude()),
                request.getLongitude().orElse(store.getLongitude()),
                request.getPhone().orElse(store.getStorePhone()),
                request.getIntroduction().orElse(store.getIntroduction()),
                request.getOperatingHours().orElse(store.getOperatingHours()),

                request.getStoreCategories().isPresent()
                        ? (request.getStoreCategories().get() == null ? new HashSet<>() : new HashSet<>(request.getStoreCategories().get()))
                        : null,

                request.getStoreMoods().isPresent()
                        ? (request.getStoreMoods().get() == null ? new HashSet<>() : new HashSet<>(request.getStoreMoods().get()))
                        : null,

                request.getHolidayDates().orElse(store.getHolidayDates()),
                request.getIsSuspended().orElse(store.getIsSuspended()),
                request.getRepresentativeName().orElse(store.getRepresentativeName()),
                profileImageUrl
        );

        // 갤러리 이미지 처리
        if (request.getImageUrls().isPresent()) {
            List<String> desiredUrls = request.getImageUrls().get() != null
                    ? request.getImageUrls().get() : Collections.emptyList();

            validateImageLimit(desiredUrls, 3, "일반 이미지는 최대 3장까지 등록할 수 있습니다.");

            s3Service.syncImages(
                    store::getImages,
                    desiredUrls,
                    store::removeImage,
                    url -> StoreImage.builder().store(store).imageUrl(url).orderIndex(0).build(),
                    store::addImage
            );
        }

        if (request.getMenuBoardImageUrls().isPresent()) {
            List<String> desiredMenuBoardUrls = request.getMenuBoardImageUrls().get() != null
                    ? request.getMenuBoardImageUrls().get() : Collections.emptyList();

            validateImageLimit(desiredMenuBoardUrls, 10, "메뉴판 이미지는 최대 10장까지 등록할 수 있습니다.");

            s3Service.syncImages(
                    store::getMenuBoardImages,
                    desiredMenuBoardUrls,
                    store::removeMenuBoardImage,
                    url -> MenuBoardImage.builder().imageUrl(url).orderIndex(0).build(),
                    store::addMenuBoardImage
            );
        }

        recalculateCloverGrade(store);
    }

    // 상점 삭제
    @Transactional
    public void deleteStoreForOwner(Long storeId, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        if (store.getStoreStatus() == StoreStatus.ACTIVE) {
            if (owner.getRole() == Role.ROLE_ADMIN) {
                throw new CustomException(ErrorCode.FORBIDDEN, "점유된 상점은 관리자가 삭제할 수 없습니다.");
            }
            if (!Objects.equals(store.getUser().getId(), owner.getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
            }
        } else {
            if (owner.getRole() != Role.ROLE_ADMIN) {
                throw new CustomException(ErrorCode.FORBIDDEN, "해당 상점은 관리자만 삭제할 수 있습니다.");
            }
        }

        deleteStoreAssets(store);
        storeRepository.delete(store);
    }

    // 상점 통계 조회
    public StoreStatsResponse getStoreStatsForOwner(Long storeId, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
        }

        long totalRegulars = favoriteRepository.countByStore(store);
        long totalReviews = reviewRepository.countByStoreIdAndParentReviewIsNull(storeId);
        long totalIssuedCoupons = couponRepository.countByStoreId(storeId);
        long totalUsedCoupons = studentCouponRepository.countByCoupon_StoreIdAndStatus(storeId, CouponUsageStatus.USED);
        long favoriteIncreaseCount = favoriteRepository.countByStoreAndCreatedAtAfter(store, LocalDateTime.now(ZoneId.of("Asia/Seoul")).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0));

        return StoreStatsResponse.builder()
                .totalRegulars(totalRegulars)
                .totalIssuedCoupons(totalIssuedCoupons)
                .totalUsedCoupons(totalUsedCoupons)
                .totalReviews(totalReviews)
                .favoriteIncreaseCount(favoriteIncreaseCount)
                .build();
    }

    // 상점 등록 상태 조회
    public StoreRegistrationStatusResponse getStoreRegistrationStatusForOwner(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        boolean hasMenu = itemRepository.existsByStoreId(storeId);
        boolean hasStoreInfo = StringUtils.hasText(store.getIntroduction())
                && store.getStoreCategories() != null && !store.getStoreCategories().isEmpty()
                && store.getStoreMoods() != null && !store.getStoreMoods().isEmpty();

        return StoreRegistrationStatusResponse.of(hasMenu, hasStoreInfo);
    }

    // 내 상점 조회
    public List<OwnerStoreResponse> getMyStoresForOwner(User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Store> stores = storeRepository.findAllByUser(owner);
        return stores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            return OwnerStoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, store.getCloverGrade());
        }).toList();
    }

    // --- 학생용 ---

    public StudentStoreResponse getStoreForStudent(Long storeId, User user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상점을 찾을 수 없습니다."));

        Double averageRating = reviewRepository.findAverageRatingByStoreId(storeId);
        Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(storeId);

        Map<Long, List<PartnershipInfo>> partnershipsMap = partnershipService.getMyPartnershipOrganizations(List.of(storeId), user);
        List<PartnershipInfo> myPartnerships = partnershipsMap.getOrDefault(storeId, List.of());

        return StudentStoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, store.getCloverGrade());
    }

    public PageResponse<StudentStoreResponse> getStoresForStudent(String keyword, List<StoreCategory> categories, List<StoreMood> moods, Long universityId, Boolean hasPartnership, StoreStatus storeStatus, Pageable pageable, User user) {
        Specification<Store> spec = Specification.where(StoreSpecification.hasKeyword(keyword))
                .and(StoreSpecification.hasCategories(categories))
                .and(StoreSpecification.hasMoods(moods))
                .and(StoreSpecification.hasUniversityId(universityId))
                .and(StoreSpecification.hasPartnership(hasPartnership))
                .and(StoreSpecification.hasStoreStatus(storeStatus))
                .and(StoreSpecification.isNotSuspended());

        Page<Store> storePage = storeRepository.findAll(spec, pageable);
        List<Long> storeIds = storePage.getContent().stream().map(Store::getId).toList();

        Map<Long, List<PartnershipInfo>> partnershipMap = partnershipService.getMyPartnershipOrganizations(storeIds, user);

        Page<StudentStoreResponse> responsePage = storePage.map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            List<PartnershipInfo> myPartnerships = partnershipMap.getOrDefault(store.getId(), List.of());
            return StudentStoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, store.getCloverGrade());
        });

        return PageResponse.from(responsePage);
    }

    public List<StudentStoreResponse> getNearbyStoresForStudent(Double latitude, Double longitude, Double radius, User user) {
        List<Store> stores = storeRepository.findByLocationWithin(latitude, longitude, radius);
        List<Long> storeIds = stores.stream().map(Store::getId).toList();

        Map<Long, List<PartnershipInfo>> partnershipMap = partnershipService.getMyPartnershipOrganizations(storeIds, user);

        return stores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            List<PartnershipInfo> myPartnerships = partnershipMap.getOrDefault(store.getId(), List.of());
            return StudentStoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, store.getCloverGrade());
        }).toList();
    }

    public List<StudentStoreResponse> getStoresByLocationForStudent(Double latitude, Double longitude, User user) {
        List<Store> stores = storeRepository.findByLatitudeAndLongitude(latitude, longitude);
        List<Long> storeIds = stores.stream().map(Store::getId).toList();

        Map<Long, List<PartnershipInfo>> partnershipMap = partnershipService.getMyPartnershipOrganizations(storeIds, user);

        return stores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            List<PartnershipInfo> myPartnerships = partnershipMap.getOrDefault(store.getId(), List.of());
            return StudentStoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, store.getCloverGrade());
        }).toList();
    }

    public List<StudentStoreMapResponse> getStoreMapForStudent(Long universityId, User user) {
        Specification<Store> spec = Specification.where(StoreSpecification.isNotSuspended());

        if (universityId != null) {
            spec = spec.and(StoreSpecification.hasUniversityId(universityId));
        }

        List<Store> stores = storeRepository.findAll(spec);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate today = now.toLocalDate();

        StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);

        Long filterUniversityId = null;
        if (studentProfile != null && studentProfile.getUniversity() != null) {
            filterUniversityId = studentProfile.getUniversity().getId();
        } else if (universityId != null) {
            filterUniversityId = universityId;
        }

        Set<Long> unclaimedStoreIdsWithPartnership = new HashSet<>();
        if (filterUniversityId != null) {
            List<Long> unclaimedStoreIds = stores.stream()
                    .filter(s -> s.getStoreStatus() == StoreStatus.UNCLAIMED)
                    .map(Store::getId)
                    .toList();
            if (!unclaimedStoreIds.isEmpty()) {
                unclaimedStoreIdsWithPartnership = new HashSet<>(
                        partnershipRepository.findStoreIdsWithActivePartnershipsByUniversityId(
                                unclaimedStoreIds, filterUniversityId, today)
                );
            }
        }

        final Set<Long> finalUnclaimedWithPartnership = unclaimedStoreIdsWithPartnership;
        final Long finalFilterUniversityId = filterUniversityId;

        List<Store> filteredStores = stores.stream()
                .filter(store -> store.getStoreStatus() != StoreStatus.UNCLAIMED
                        || finalFilterUniversityId == null
                        || finalUnclaimedWithPartnership.contains(store.getId()))
                .toList();

        List<Long> filteredStoreIds = filteredStores.stream().map(Store::getId).toList();

        Map<Long, List<PartnershipInfo>> partnershipMap = partnershipService.getMyPartnershipOrganizations(filteredStoreIds, user);

        Set<Long> couponStoreIds = new HashSet<>();
        if (studentProfile != null && studentProfile.getUniversity() != null && !filteredStoreIds.isEmpty()) {
            couponStoreIds = new HashSet<>(couponRepository.findStoreIdsWithDownloadableCoupons(filteredStoreIds, now, user.getId()));
        }

        final Set<Long> finalCouponStoreIds = couponStoreIds;

        return filteredStores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            Long favoriteCount = favoriteRepository.countByStore(store);
            List<PartnershipInfo> myPartnerships = partnershipMap.getOrDefault(store.getId(), List.of());
            boolean hasCoupon = finalCouponStoreIds.contains(store.getId());
            return StudentStoreMapResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, hasCoupon, favoriteCount);
        }).toList();
    }

    public List<HotStoreResponse> getHotStoresForStudent(User user) {
        StudentProfile studentProfile = studentProfileRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "학생 회원만 이용 가능합니다."));

        if (studentProfile.getUniversity() == null) {
            throw new CustomException(ErrorCode.FORBIDDEN, "소속 대학이 없습니다.");
        }

        Long universityId = studentProfile.getUniversity().getId();

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate today = now.toLocalDate();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<Object[]> results = favoriteRepository.findHotStores(universityId, startOfWeek, endOfWeek, Pageable.ofSize(10));

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<Store> stores = results.stream()
                .map(result -> (Store) result[0])
                .toList();
        List<Long> storeIds = stores.stream().map(Store::getId).toList();

        List<Partnership> partnerships = partnershipRepository.findActivePartnershipsByStoreIdsAndUniversityId(storeIds, universityId, today);
        Map<Long, List<Partnership>> partnershipMap = partnerships.stream()
                .collect(Collectors.groupingBy(p -> p.getStore().getId()));

        List<Coupon> coupons = couponRepository.findActiveCouponsByStoreIds(storeIds, now);
        Map<Long, List<Coupon>> couponMap = coupons.stream()
                .collect(Collectors.groupingBy(c -> c.getStore().getId()));

        return results.stream()
                .map(result -> {
                    Store store = (Store) result[0];
                    Long count = (Long) result[1];
                    Long sId = store.getId();
                    String benefitContent = null;

                    if (partnershipMap.containsKey(sId) && !partnershipMap.get(sId).isEmpty()) {
                        benefitContent = partnershipMap.get(sId).get(0).getBenefit();
                    } else if (couponMap.containsKey(sId) && !couponMap.get(sId).isEmpty()) {
                        benefitContent = couponMap.get(sId).get(0).getTitle();
                    }

                    return HotStoreResponse.from(store, count, benefitContent);
                })
                .toList();
    }

    @Transactional
    public void reportStoreForStudent(Long storeId, Long reporterId, StoreReportRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        User reporter = userRepository.getReferenceById(reporterId);

        if (storeReportRepository.existsByStoreAndReporter(store, reporter)) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "이미 신고한 상점입니다.");
        }

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

    // -- 내부 메서드 --

    @Transactional
    public void recalculateCloverGrade(Store store) {
        if (store.getUser() == null) {
            store.updateCloverGrade(CloverGrade.SEED);
            return;
        }

        boolean hasStoreInfo = StringUtils.hasText(store.getIntroduction())
                && StringUtils.hasText(store.getOperatingHours())
                && !store.getStoreCategories().isEmpty()
                && !store.getStoreMoods().isEmpty();

        boolean hasMenu = itemRepository.existsByStoreId(store.getId());

        if (hasStoreInfo && hasMenu) {
            store.updateCloverGrade(CloverGrade.THREE_LEAF);
        } else {
            store.updateCloverGrade(CloverGrade.SPROUT);
        }
    }

    public void validateStoreOwner(Store store, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (store.getStoreStatus() == StoreStatus.UNCLAIMED && owner.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "가게 주인이 아닙니다.");
        }
    }

    private String normalizeBranch(String branch) {
        return branch == null ? "" : branch.trim();
    }

    private String sanitizeBranch(String branch) {
        String normalizedBranch = normalizeBranch(branch);
        return normalizedBranch.isEmpty() ? null : normalizedBranch;
    }

    private void validateImageLimit(List<String> imageUrls, int limit, String message) {
        if (imageUrls != null && imageUrls.size() > limit) {
            throw new CustomException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private void addStoreImages(Store store, List<String> imageUrls) {
        if (imageUrls == null) {
            return;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            store.addImage(StoreImage.builder()
                    .store(store)
                    .imageUrl(imageUrls.get(i))
                    .orderIndex(i)
                    .build());
        }
    }

    private void addMenuBoardImages(Store store, List<String> imageUrls) {
        if (imageUrls == null) {
            return;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            store.addMenuBoardImage(MenuBoardImage.builder()
                    .imageUrl(imageUrls.get(i))
                    .orderIndex(i)
                    .build());
        }
    }

    private void deleteStoreAssets(Store store) {
        s3Service.deleteFile(store.getProfileImageUrl());
        store.getImages().forEach(image -> s3Service.deleteFile(image.getImageUrl()));
        store.getMenuBoardImages().forEach(image -> s3Service.deleteFile(image.getImageUrl()));
    }

}
