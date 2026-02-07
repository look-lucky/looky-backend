package com.looky.domain.store.service;

import com.looky.common.service.S3Service;
import com.looky.common.util.FileValidator;
import com.looky.domain.coupon.entity.CouponUsageStatus;
import com.looky.domain.store.entity.*;
import com.looky.domain.store.repository.StoreSpecification;
import org.springframework.data.jpa.domain.Specification;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.response.PageResponse;
import com.looky.domain.store.dto.*;

import java.util.HashSet;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
import com.looky.domain.coupon.repository.CouponRepository;
import com.looky.domain.partnership.entity.Partnership;
import com.looky.domain.coupon.entity.Coupon;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import com.looky.domain.coupon.repository.StudentCouponRepository;

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
    private final CouponRepository couponRepository;
    private final StudentCouponRepository studentCouponRepository;

    @Transactional
    public Long createStore(User user, CreateStoreRequest request, List<MultipartFile> images) throws IOException {

        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (owner.getRole() != Role.ROLE_OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN, "점주 회원만 가게를 등록할 수 있습니다.");
        }


        if (storeRepository.existsByName(request.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 상점 이름입니다.");
        }

        if (storeRepository.existsByBizRegNo(request.getBizRegNo())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 사업자등록번호입니다.");
        }

        // 이미지 유효성 검사 (최대 3장, 10MB)
        FileValidator.validateImageFiles(images, 3, 10 * 1024 * 1024);

        Store store = request.toEntity(owner);

        // 이미지 S3 업로드 및 리스트 순서대로 DB 저장
        uploadAndSaveImages(store, images);

        Store savedStore = storeRepository.save(store);

        // 초기 등급 계산 (SEED 할당)
        recalculateCloverGrade(savedStore);

        return savedStore.getId();
    }

    public StoreResponse getStore(Long storeId, User user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상점을 찾을 수 없습니다."));

        Double averageRating = reviewRepository.findAverageRatingByStoreId(storeId);
        Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(storeId);

        boolean isPartnership = false;
        boolean hasCoupon = false;

        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);
            // 학생이고 소속 대학이 있는 경우
            if (studentProfile != null && studentProfile.getUniversity() != null) {
                Long universityId = studentProfile.getUniversity().getId();
                LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
                LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

                // 해당 상점과 제휴 여부 확인
                isPartnership = partnershipRepository.existsActivePartnership(storeId, universityId, today);
                // 해당 상점의 쿠폰 보유 여부 확인
                hasCoupon = couponRepository.existsActiveCoupon(storeId, now);
            }
        }

        return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, isPartnership, hasCoupon, store.getCloverGrade());
    }

    public PageResponse<StoreResponse> getStores(String keyword, List<StoreCategory> categories, List<StoreMood> moods, Long universityId, Pageable pageable, User user) {
        Specification<Store> spec = Specification.where(StoreSpecification.hasKeyword(keyword))
                .and(StoreSpecification.hasCategories(categories))
                .and(StoreSpecification.hasMoods(moods))
                .and(StoreSpecification.hasUniversityId(universityId))
                .and(StoreSpecification.isNotSuspended());

        Page<Store> storePage = storeRepository.findAll(spec, pageable);

        // 배치 최적화를 위한 정보 준비
        Long userUniversityId = null;
        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);
            // 학생이고 소속 대학이 있는 경우 대학 ID 추출
            if (studentProfile != null && studentProfile.getUniversity() != null) {
                userUniversityId = studentProfile.getUniversity().getId();
            }
        }

        final Long finalUserUniversityId = userUniversityId;
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        
        List<Long> storeIds = storePage.getContent().stream().map(Store::getId).toList();
        
        
        // 정확한 배치를 위해 findActivePartnershipsByStoreIdsAndUniversityId(제휴 정보 리스트 반환)를 사용한 후 상점 ID 추출
        Set<Long> batchedPartnershipStoreIds = new HashSet<>();
        // 학생 회원의 경우, 조회된 상점 목록에 대해 일괄적으로 제휴 여부를 확인 (N+1 방지)
        if (finalUserUniversityId != null && !storeIds.isEmpty()) {
            batchedPartnershipStoreIds = partnershipRepository.findActivePartnershipsByStoreIdsAndUniversityId(storeIds, finalUserUniversityId, today)
                    .stream().map(p -> p.getStore().getId()).collect(Collectors.toSet());
        }

        Set<Long> batchedCouponStoreIds = new HashSet<>();
        // 학생 회원의 경우, 조회된 상점 목록에 대해 일괄적으로 쿠폰 보유 여부를 확인 (N+1 방지)
        if (finalUserUniversityId != null && !storeIds.isEmpty()) {
             batchedCouponStoreIds = couponRepository.findActiveCouponsByStoreIds(storeIds, now)
                     .stream().map(c -> c.getStore().getId()).collect(Collectors.toSet());
        }

        // 람다 표현식에서 사용하기 위해 effectively final 변수로 선언
        final Set<Long> finalPartnershipStoreIds = batchedPartnershipStoreIds;
        final Set<Long> finalCouponStoreIds = batchedCouponStoreIds;

        Page<StoreResponse> responsePage = storePage.map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            
            // 제휴 여부 및 쿠폰 보유 여부 설정
            boolean isPartnership = finalPartnershipStoreIds.contains(store.getId());
            boolean hasCoupon = finalCouponStoreIds.contains(store.getId());

            return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, isPartnership, hasCoupon, store.getCloverGrade());
        });
        return PageResponse.from(responsePage);
    }

        public StoreStatsResponse getStoreStats(Long storeId, User user) {
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

        return StoreStatsResponse.builder()
                .totalRegulars(totalRegulars)
                .totalIssuedCoupons(totalIssuedCoupons)
                .totalUsedCoupons(totalUsedCoupons)
                .totalReviews(totalReviews)
                .build();
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

        // 새 이미지 유효성 검사 (최대 3장, 10MB)
        if (images != null && !images.isEmpty()) {
            FileValidator.validateImageFiles(images, 3, 10 * 1024 * 1024);
        }

        store.updateStore(
            request.getName(),
            request.getBranch(),
            request.getRoadAddress(),
            request.getJibunAddress(),
            request.getLatitude(),
            request.getLongitude(),
            request.getPhone(),
            request.getIntroduction(),
            request.getOperatingHours(),
            request.getStoreCategories() != null ? new HashSet<>(request.getStoreCategories()) : null,
            request.getStoreMoods() != null ? new HashSet<>(request.getStoreMoods()) : null,
            request.getHolidayStartsAt(),
            request.getHolidayEndsAt(),
            request.getIsSuspended()
        );

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
        
        // 등급 재계산
        recalculateCloverGrade(store);
    }

    // 위치 기반 상점 목록 조회
    public List<StoreResponse> getNearbyStores(Double latitude, Double longitude, Double radius, User user) {
        List<Store> stores = storeRepository.findByLocationWithin(latitude, longitude, radius);
        
        Long userUniversityId = null;
        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);
            if (studentProfile != null && studentProfile.getUniversity() != null) {
                userUniversityId = studentProfile.getUniversity().getId();
            }
        }
        
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        
        List<Long> storeIds = stores.stream().map(Store::getId).toList();
        
        Set<Long> batchedPartnershipStoreIds = new HashSet<>();
        if (userUniversityId != null && !storeIds.isEmpty()) {
            batchedPartnershipStoreIds = partnershipRepository.findActivePartnershipsByStoreIdsAndUniversityId(storeIds, userUniversityId, today)
                    .stream().map(p -> p.getStore().getId()).collect(Collectors.toSet());
        }

        Set<Long> batchedCouponStoreIds = new HashSet<>();
        if (userUniversityId != null && !storeIds.isEmpty()) {
             batchedCouponStoreIds = couponRepository.findActiveCouponsByStoreIds(storeIds, now)
                     .stream().map(c -> c.getStore().getId()).collect(Collectors.toSet());
        }

        final Set<Long> finalPartnershipStoreIds = batchedPartnershipStoreIds;
        final Set<Long> finalCouponStoreIds = batchedCouponStoreIds;
        
        return stores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            
            boolean isPartnership = finalPartnershipStoreIds.contains(store.getId());
            boolean hasCoupon = finalCouponStoreIds.contains(store.getId());
            
            return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, isPartnership, hasCoupon, store.getCloverGrade());
        }).toList();
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
        return stores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());

            return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, false, false, store.getCloverGrade());
        }).toList();
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


    // 상점 등록 상태 조회 (메뉴 정보 등록 여부, 매장 정보 등록 여부)
    public StoreRegistrationStatusResponse getStoreRegistrationStatus(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        boolean hasMenu = itemRepository.existsByStoreId(storeId);
        boolean hasStoreInfo = StringUtils.hasText(store.getIntroduction()) 
                && store.getStoreCategories() != null && !store.getStoreCategories().isEmpty()
                && store.getStoreMoods() != null && !store.getStoreMoods().isEmpty();

        return StoreRegistrationStatusResponse.of(hasMenu, hasStoreInfo);
    }

    public List<HotStoreResponse> getHotStores(User user) {
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

        // 1. 핫한 상점 조회 (Object[]: Store, Long count) - 이번 주 찜이 가장 많이 늘어난 상점 Top 10 조회
        List<Object[]> results = favoriteRepository.findHotStores(universityId, startOfWeek, endOfWeek, Pageable.ofSize(10));

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<Store> stores = results.stream()
                .map(result -> (Store) result[0])
                .toList();
        List<Long> storeIds = stores.stream().map(Store::getId).toList();

        // 2. 활성화된 제휴 정보 일괄 조회 (N+1 방지)
        // 해당 대학과 제휴 맺은 상점들의 현재 유효한 제휴 정보를 조회
        List<Partnership> partnerships = partnershipRepository.findActivePartnershipsByStoreIdsAndUniversityId(storeIds, universityId, today);
        Map<Long, List<Partnership>> partnershipMap = partnerships.stream()
                .collect(Collectors.groupingBy(p -> p.getStore().getId()));

        // 3. 활성화된 쿠폰 정보 일괄 조회 (N+1 방지)
        // 상점들의 현재 유효한 쿠폰 정보를 조회
        List<Coupon> coupons = couponRepository.findActiveCouponsByStoreIds(storeIds, now);
        Map<Long, List<Coupon>> couponMap = coupons.stream()
                .collect(Collectors.groupingBy(c -> c.getStore().getId()));


        // 4. 응답 객체로 매핑 (혜택 우선순위 적용)
        return results.stream()
                .map(result -> {
                    Store store = (Store) result[0];
                    Long count = (Long) result[1];
                    Long storeId = store.getId();
                    String benefitContent = null;

                    // 우선순위 1: 제휴 혜택 (소속 대학과 제휴된 경우)
                    if (partnershipMap.containsKey(storeId) && !partnershipMap.get(storeId).isEmpty()) {
                        benefitContent = partnershipMap.get(storeId).get(0).getBenefit();
                    } 
                    // 우선순위 2: 쿠폰 혜택 (제휴가 없고, 발급 가능한 쿠폰이 있는 경우)
                    else if (couponMap.containsKey(storeId) && !couponMap.get(storeId).isEmpty()) {
                        benefitContent = couponMap.get(storeId).get(0).getTitle();
                    }

                    return HotStoreResponse.from(store, count, benefitContent);
                })
                .toList();
    }

    // 클로버 등급 재계산 및 업데이트
    @Transactional
    public void recalculateCloverGrade(Store store) {

        if (store.getUser() == null) {
            store.updateCloverGrade(CloverGrade.SEED);
            return;
        }

        // 매장 정보: 소개글(introduction), 운영시간(operatingHours) 등 필수 정보 확인.
        // 메뉴 정보: ItemRepository를 통해 확인.
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
}
