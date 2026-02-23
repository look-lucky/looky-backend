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
import com.looky.domain.partnership.dto.PartnershipInfo;

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
import com.looky.domain.partnership.service.PartnershipService;
import com.looky.domain.coupon.repository.CouponRepository;
import com.looky.domain.partnership.entity.Partnership;
import com.looky.domain.coupon.entity.Coupon;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
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

    @Transactional
    public Long createStore(User user, StoreCreateRequest request, List<MultipartFile> images) throws IOException {

        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (owner.getRole() != Role.ROLE_OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN, "점주 회원만 가게를 등록할 수 있습니다.");
        }


        if (storeRepository.existsByNameAndRoadAddress(request.getName(), request.getRoadAddress())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 상점입니다.");
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

        // 대학 연결
        if (request.getUniversityIds() != null) {
            for (Long universityId : request.getUniversityIds()) {
                universityRepository.findById(universityId)
                        .ifPresent(savedStore::addUniversity);
            }
        }

        // 초기 등급 계산 (SEED 할당)
        recalculateCloverGrade(savedStore);

        return savedStore.getId();
    }

    public StoreResponse getStore(Long storeId, User user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상점을 찾을 수 없습니다."));

        Double averageRating = reviewRepository.findAverageRatingByStoreId(storeId);
        Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(storeId);

        List<PartnershipInfo> myPartnerships = null;
        boolean hasCoupon = false;

        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);
            // 학생이고 소속 대학이 있는 경우
            if (studentProfile != null && studentProfile.getUniversity() != null) {
                LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
                
                // 내 혜택 조회
                Map<Long, List<PartnershipInfo>> partnershipsMap = partnershipService.getMyPartnershipOrganizations(List.of(storeId), user);
                myPartnerships = partnershipsMap.get(storeId);
                
                // 해당 상점의 쿠폰 보유 여부 확인
                hasCoupon = couponRepository.existsActiveCoupon(storeId, now);
            }
        }

        return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, hasCoupon, store.getCloverGrade());
    }

    public PageResponse<StoreResponse> getStores(String keyword, List<StoreCategory> categories, List<StoreMood> moods, Long universityId, Boolean hasPartnership, Pageable pageable, User user) {
        Specification<Store> spec = Specification.where(StoreSpecification.hasKeyword(keyword))
                .and(StoreSpecification.hasCategories(categories))
                .and(StoreSpecification.hasMoods(moods))
                .and(StoreSpecification.hasUniversityId(universityId))
                .and(StoreSpecification.hasPartnership(hasPartnership))
                .and(StoreSpecification.isNotSuspended());

        Page<Store> storePage = storeRepository.findAll(spec, pageable);

        // 배치 최적화를 위한 정보 준비
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        
        List<Long> storeIds = storePage.getContent().stream().map(Store::getId).toList();
        
        // 상점 제휴-내 소속 매칭 조직 이름 목록 생성
        Map<Long, List<PartnershipInfo>> partnershipMap = partnershipService.getMyPartnershipOrganizations(storeIds, user);

        Set<Long> batchedCouponStoreIds = new HashSet<>();
        // 학생 회원의 경우, 조회된 상점 목록에 대해 일괄적으로 쿠폰 보유 여부를 확인 (N+1 방지)
        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);
            if (studentProfile != null && studentProfile.getUniversity() != null) {
                 batchedCouponStoreIds = couponRepository.findActiveCouponsByStoreIds(storeIds, now)
                         .stream().map(c -> c.getStore().getId()).collect(Collectors.toSet());
            }
        }

        // 람다 표현식에서 사용하기 위해 effectively final 변수로 선언
        final Set<Long> finalCouponStoreIds = batchedCouponStoreIds;

        Page<StoreResponse> responsePage = storePage.map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            
            // 제휴 여부 및 쿠폰 보유 여부 설정
            List<PartnershipInfo> myPartnerships = partnershipMap.get(store.getId());
            boolean hasCoupon = finalCouponStoreIds.contains(store.getId());

            return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, hasCoupon, store.getCloverGrade());
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
        long favoriteIncreaseCount = favoriteRepository.countByStoreAndCreatedAtAfter(store, LocalDateTime.now(ZoneId.of("Asia/Seoul")).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0));

        return StoreStatsResponse.builder()
                .totalRegulars(totalRegulars)
                .totalIssuedCoupons(totalIssuedCoupons)
                .totalUsedCoupons(totalUsedCoupons)
                .totalReviews(totalReviews)
                .favoriteIncreaseCount(favoriteIncreaseCount)
                .build();
    }
    
    @Transactional
    public void updateStore(Long storeId, User user, StoreUpdateRequest request, List<MultipartFile> images)
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

        if (request.getName().isPresent() && request.getName().get() != null && !store.getName().equals(request.getName().get()) && storeRepository.existsByName(request.getName().get())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 상점 이름입니다.");
        }

        store.updateStore(
            request.getName().orElse(store.getName()),
            request.getBranch().orElse(store.getBranch()),
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
            request.getRepresentativeName().orElse(store.getRepresentativeName())
        );

        // 1. 이미지 삭제 처리 (preserveImageIds 기준)
        if (request.getPreserveImageIds().isPresent()) {
            List<Long> preserveIds = request.getPreserveImageIds().get();
            // preserveIds가 null이면 빈 리스트로 처리 (모두 삭제)
            List<Long> finalPreserveIds = preserveIds != null ? preserveIds : Collections.emptyList();

            List<StoreImage> imagesToDelete = store.getImages().stream()
                    .filter(img -> !finalPreserveIds.contains(img.getId()))
                    .toList();

            for (StoreImage img : imagesToDelete) {
                s3Service.deleteFile(img.getImageUrl());
                store.removeImage(img);
            }
        }

        // 2. 새 이미지 추가 및 전체 개수 검증
        int currentImageCount = store.getImages().size(); // 삭제 후 남은 개수
        int newImageCount = (images != null) ? images.size() : 0;

        if (currentImageCount + newImageCount > 3) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 3장까지 등록할 수 있습니다.");
        }

        // 새 이미지 업로드 및 저장
        if (newImageCount > 0) {
            FileValidator.validateImageFiles(images, 3, 10 * 1024 * 1024);
            uploadAndSaveImages(store, images);
        }

        // 3. 인덱스 재정렬 (중간 삭제 시 빈 번호 채우기 위함)
        for (int i = 0; i < store.getImages().size(); i++) {
             store.getImages().get(i).updateOrderIndex(i);
        }
        
        // 등급 재계산
        recalculateCloverGrade(store);
    }

    // 위치 기반 상점 목록 조회
    public List<StoreResponse> getNearbyStores(Double latitude, Double longitude, Double radius, User user) {
        List<Store> stores = storeRepository.findByLocationWithin(latitude, longitude, radius);
        
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        
        List<Long> storeIds = stores.stream().map(Store::getId).toList();
        
        Map<Long, List<PartnershipInfo>> partnershipMap = partnershipService.getMyPartnershipOrganizations(storeIds, user);

        Set<Long> batchedCouponStoreIds = new HashSet<>();
        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);
            if (studentProfile != null && studentProfile.getUniversity() != null && !storeIds.isEmpty()) {
                 batchedCouponStoreIds = couponRepository.findActiveCouponsByStoreIds(storeIds, now)
                         .stream().map(c -> c.getStore().getId()).collect(Collectors.toSet());
            }
        }

        final Set<Long> finalCouponStoreIds = batchedCouponStoreIds;
        
        return stores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            
            List<PartnershipInfo> myPartnerships = partnershipMap.get(store.getId());
            boolean hasCoupon = finalCouponStoreIds.contains(store.getId());
            
            return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, hasCoupon, store.getCloverGrade());
        }).toList();
    }

    // 특정 위치 상점 목록 조회 (같은 건물 상점 목록 조회)
    public List<StoreResponse> getStoresByLocation(Double latitude, Double longitude, User user) {
        List<Store> stores = storeRepository.findByLatitudeAndLongitude(latitude, longitude);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        List<Long> storeIds = stores.stream().map(Store::getId).toList();

        Map<Long, List<PartnershipInfo>> partnershipMap = partnershipService.getMyPartnershipOrganizations(storeIds, user);

        Set<Long> batchedCouponStoreIds = new HashSet<>();
        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);
            if (studentProfile != null && studentProfile.getUniversity() != null && !storeIds.isEmpty()) {
                batchedCouponStoreIds = couponRepository.findActiveCouponsByStoreIds(storeIds, now)
                        .stream().map(c -> c.getStore().getId()).collect(Collectors.toSet());
            }
        }

        final Set<Long> finalCouponStoreIds = batchedCouponStoreIds;

        return stores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());

            List<PartnershipInfo> myPartnerships = partnershipMap.get(store.getId());
            boolean hasCoupon = finalCouponStoreIds.contains(store.getId());

            return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, hasCoupon, store.getCloverGrade());
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

            return StoreResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, null, false, store.getCloverGrade());
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

    // 지도에 필요한 상점 목록 조회 (대학 기반)
    public List<StoreMapResponse> getStoreMap(Long universityId, User user) {
        // 대학 ID 필터링 적용 (StoreSpecification 활용)
        Specification<Store> spec = Specification.where(StoreSpecification.isNotSuspended());

        if (universityId != null) {
            spec = spec.and(StoreSpecification.hasUniversityId(universityId));
        }

        List<Store> stores = storeRepository.findAll(spec);
        List<Long> storeIds = stores.stream().map(Store::getId).toList();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 2. 활성화된 제휴 정보 일괄 조회 (N+1 방지)
        Map<Long, List<PartnershipInfo>> partnershipMap = partnershipService.getMyPartnershipOrganizations(storeIds, user);

        Set<Long> batchedCouponStoreIds = new HashSet<>();

        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile studentProfile = studentProfileRepository.findById(user.getId()).orElse(null);
            if (studentProfile != null && studentProfile.getUniversity() != null && !storeIds.isEmpty()) {
                batchedCouponStoreIds = couponRepository.findActiveCouponsByStoreIds(storeIds, now)
                        .stream().map(c -> c.getStore().getId()).collect(Collectors.toSet());
            }
        }

        final Set<Long> finalCouponStoreIds = batchedCouponStoreIds;

        return stores.stream().map(store -> {
            Double averageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
            Long reviewCount = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
            Long favoriteCount = favoriteRepository.countByStore(store);

            List<PartnershipInfo> myPartnerships = partnershipMap.get(store.getId());
            boolean hasCoupon = finalCouponStoreIds.contains(store.getId());

            return StoreMapResponse.of(store, averageRating, reviewCount != null ? reviewCount.intValue() : 0, myPartnerships, hasCoupon, favoriteCount);}).toList();
    }
}
