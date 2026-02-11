package com.looky.domain.coupon.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.coupon.dto.*;
import com.looky.domain.coupon.entity.*;
import com.looky.domain.coupon.repository.*;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.looky.domain.user.entity.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.looky.domain.user.entity.StudentProfile;
import com.looky.domain.user.repository.StudentProfileRepository;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final StudentCouponRepository studentCouponRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    // --- 점주용 ---

    @Transactional
    public Long createCoupon(Long storeId, User user, CreateCouponRequest request) {
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, user);

        Coupon coupon = Coupon.builder()
                .store(store)
                .title(request.getTitle())
                .issueStartsAt(request.getIssueStartsAt())
                .issueEndsAt(request.getIssueEndsAt())
                .totalQuantity(request.getTotalQuantity())
                .limitPerUser(request.getLimitPerUser())
                .status(request.getStatus() != null ? request.getStatus() : CouponStatus.ACTIVE)
                .benefitType(request.getBenefitType())
                .benefitValue(request.getBenefitValue())
                .minOrderAmount(request.getMinOrderAmount())
                .build();

        if (request.getIssueStartsAt() != null && request.getIssueEndsAt() != null) {
            if (request.getIssueEndsAt().isBefore(request.getIssueStartsAt())) {
                throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "종료일은 시작일보다 빠를 수 없습니다.");
            }
        }


        Coupon savedCoupon = couponRepository.save(coupon);

        return savedCoupon.getId();
    }

    @Transactional
    public void updateCoupon(Long couponId, User user, UpdateCouponRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        validateStoreOwner(coupon.getStore(), user);

        coupon.updateCoupon(
                request.getTitle().orElse(coupon.getTitle()),
                request.getIssueStartsAt().orElse(coupon.getIssueStartsAt()),
                request.getIssueEndsAt().orElse(coupon.getIssueEndsAt()),
                request.getTotalQuantity().orElse(coupon.getTotalQuantity()),
                request.getLimitPerUser().orElse(coupon.getLimitPerUser()),
                request.getStatus().orElse(coupon.getStatus()),
                request.getBenefitType().orElse(coupon.getBenefitType()),
                request.getBenefitValue().orElse(coupon.getBenefitValue()),
                request.getMinOrderAmount().orElse(coupon.getMinOrderAmount()));

        if (coupon.getIssueStartsAt() != null && coupon.getIssueEndsAt() != null) {
            if (coupon.getIssueEndsAt().isBefore(coupon.getIssueStartsAt())) {
                throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "종료일은 시작일보다 빠를 수 없습니다.");
            }
        }
    }

    @Transactional
    public void deleteCoupon(Long couponId, User user) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        validateStoreOwner(coupon.getStore(), user);

        couponRepository.delete(coupon);
    }

    @Transactional(readOnly = true)
    public VerifyCouponResponse verifyCouponCode(Long storeId, User owner, String verificationCode) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, owner);

        // 검증 코드로 우리 가게 활성화 쿠폰 조회
        StudentCoupon studentCoupon = studentCouponRepository.findForOwnerVerification(
                storeId, verificationCode, CouponUsageStatus.ACTIVATED
        ).orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "유효하지 않은 코드이거나 활성화되지 않은 쿠폰입니다."));

        if (studentCoupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "만료된 쿠폰입니다.");
        }

        // 학생 닉네임 조회
        String nickname = studentProfileRepository.findById(studentCoupon.getUser().getId())
                .map(StudentProfile::getNickname)
                .orElse("알 수 없는 사용자");
        
        return VerifyCouponResponse.from(studentCoupon, nickname);
    }

    @Transactional
    public void useCoupon(Long storeId, User owner, Long studentCouponId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, owner);

        StudentCoupon studentCoupon = studentCouponRepository.findById(studentCouponId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        // 내 가게 쿠폰인지 확인 (조금 더 안전하게 교차 검증)
        if (!Objects.equals(studentCoupon.getCoupon().getStore().getId(), storeId)) {
             throw new CustomException(ErrorCode.FORBIDDEN, "해당 상점의 쿠폰이 아닙니다.");
        }

        if (studentCoupon.getStatus() != CouponUsageStatus.ACTIVATED) {
             throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "활성화된 쿠폰만 사용할 수 있습니다. (현재 상태: " + studentCoupon.getStatus() + ")");
        }

        // 사용 처리
        studentCoupon.use();
    }

    // --- 학생용 ---

    // 오늘의 신규 쿠폰 조회
    public List<CouponResponse> getTodayCoupons(User user) {
        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new CustomException(ErrorCode.FORBIDDEN, "학생만 이용 가능한 서비스입니다.");
        }

        StudentProfile studentProfile = studentProfileRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생 프로필을 찾을 수 없습니다."));

        Long universityId = studentProfile.getUniversity().getId();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<Coupon> coupons = couponRepository.findTodayCouponsByUniversity(universityId, startOfDay, endOfDay, today);

        List<CouponResponse> responses = coupons.stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());

        // 발급 여부 확인
        if (!coupons.isEmpty()) {
            List<StudentCoupon> issuedCoupons = studentCouponRepository.findByUserAndCouponIn(user, coupons);
            List<Long> issuedCouponIds = issuedCoupons.stream()
                    .map(sc -> sc.getCoupon().getId())
                    .collect(Collectors.toList());

            responses.forEach(response -> {
                if (issuedCouponIds.contains(response.getId())) {
                    response.setIsDownloaded(true);
                }
            });
        }

        return responses;
    }

    public List<CouponResponse> getCouponsByStore(Long storeId, User user) {
        List<Coupon> coupons = couponRepository.findByStoreId(storeId);
        List<CouponResponse> responses = coupons.stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());

        if (!coupons.isEmpty()) {
            // 점주인 경우에만 사용 수량 조회
            if (user.getRole() == Role.ROLE_OWNER) {
                // 사용 수량 조회 (일괄)
                List<Object[]> counts = studentCouponRepository.countByCouponInAndStatus(coupons, CouponUsageStatus.USED);
                Map<Long, Long> usedCountMap = counts.stream()
                        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

                responses.forEach(response -> 
                    response.setUsedCount(usedCountMap.getOrDefault(response.getId(), 0L))
                );
            }

            // 학생인 경우에만 발급 여부 확인
            if (user.getRole() == Role.ROLE_STUDENT) {
                List<StudentCoupon> issuedCoupons = studentCouponRepository.findByUserAndCouponIn(user, coupons);
                List<Long> issuedCouponIds = issuedCoupons.stream()
                        .map(sc -> sc.getCoupon().getId())
                        .collect(Collectors.toList());

                responses.forEach(response -> {
                    if (issuedCouponIds.contains(response.getId())) {
                        response.setIsDownloaded(true);
                    }
                });
            }
        }

        return responses;
    }

    // 학생 쿠폰 발급 (다운로드)
    @Transactional
    public IssueCouponResponse downloadCoupon(Long couponId, User user) {
        // 비관적 락을 사용하여 동시성 이슈 해결 (User Limit, Total Quantity Race Condition)
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        // Validation
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "발급 가능한 상태가 아닙니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getIssueStartsAt() != null && now.isBefore(coupon.getIssueStartsAt())) {
            throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "발급 기간이 아닙니다.");
        }

        if (coupon.getIssueEndsAt() != null && now.isAfter(coupon.getIssueEndsAt())) {
            throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "발급 기간이 지났습니다.");
        }

        // 인당 발급 한도 체크 (Lock 안에서 수행되므로 안전)
        Integer userCount = studentCouponRepository.countByCouponAndUser(coupon, user);
        if (userCount >= coupon.getLimitPerUser()) {
            throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "인당 발급 한도를 초과했습니다.");
        }

        // 총 발행 한도 체크 & 증가 (Lock 안에서 수행되므로 안전)
        if (coupon.getTotalQuantity() != null && coupon.getDownloadCount() >= coupon.getTotalQuantity()) {
            throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "선착순 마감되었습니다.");
        }
        coupon.increaseDownloadCount(); // Dirty Checking으로 업데이트

        // 만료일 로직 개선: Min(발급일 + 30일, 쿠폰 종료일)
        LocalDateTime expirationDate = now.plusDays(30);
        if (coupon.getIssueEndsAt() != null && coupon.getIssueEndsAt().isBefore(expirationDate)) {
            expirationDate = coupon.getIssueEndsAt();
        }

        StudentCoupon studentCoupon = StudentCoupon.builder()
                .user(user)
                .coupon(coupon)
                .status(CouponUsageStatus.UNUSED)
                .expiresAt(expirationDate)
                .build();

        studentCouponRepository.save(studentCoupon);

        return IssueCouponResponse.from(studentCoupon, coupon.getStore().getName());
    }

    @Transactional
    public String activateCoupon(Long studentCouponId, User user) {
        StudentCoupon studentCoupon = studentCouponRepository.findByIdAndUser(studentCouponId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        if (studentCoupon.getStatus() == CouponUsageStatus.USED) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "이미 사용된 쿠폰입니다.");
        }

        if (studentCoupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "만료된 쿠폰입니다.");
        }

        // 중복 방지 코드 생성 (최대 5회 시도)
        String verificationCode = generateUniqueVerificationCode(studentCoupon.getCoupon().getStore().getId());
        
        studentCoupon.activate(verificationCode);
        return verificationCode;
    }
    
    private String generateUniqueVerificationCode(Long storeId) {
        for (int i = 0; i < 5; i++) {
            String code = String.format("%04d", java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 10000));
            if (!studentCouponRepository.existsByCoupon_Store_IdAndVerificationCodeAndStatus(storeId, code, CouponUsageStatus.ACTIVATED)) {
                return code;
            }
        }
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인증 코드 생성에 실패했습니다. 다시 시도해주세요.");
    }

    public List<IssueCouponResponse> getMyCoupons(User user) {
        return studentCouponRepository.findByUser(user).stream()
                .map(IssueCouponResponse::from)
                .collect(Collectors.toList());
    }

    private void validateStoreOwner(Store store, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "가게 주인이 아닙니다.");
        }
    }

    @Transactional
    public int resetExpiredCoupons() {
        // 현재 시간보다 30분 이전 ( = 활성화된 지 30분이 지남)
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        return studentCouponRepository.resetExpiredCoupons(threshold);
    }
}
