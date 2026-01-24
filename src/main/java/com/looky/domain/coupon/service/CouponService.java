package com.looky.domain.coupon.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.coupon.dto.*;
import com.looky.domain.coupon.entity.*;
import com.looky.domain.coupon.repository.*;
import com.looky.domain.item.entity.Item;
import com.looky.domain.item.repository.ItemRepository;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final StudentCouponRepository studentCouponRepository;
    private final CouponItemRepository couponItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrganizationRepository organizationRepository;

    // --- 점주용 ---

    @Transactional
    public Long createCoupon(Long storeId, User user, CreateCouponRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, user);

        Organization organization = null;
        if (request.getTargetOrganizationId() != null) {
            organization = organizationRepository.findById(request.getTargetOrganizationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "제휴 정보를 찾을 수 없습니다."));
        }

        Coupon coupon = Coupon.builder()
                .store(store)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetOrganization(organization)
                .issueStartsAt(request.getIssueStartsAt())
                .issueEndsAt(request.getIssueEndsAt())
                .totalQuantity(request.getTotalQuantity())
                .limitPerUser(request.getLimitPerUser())
                .status(request.getStatus() != null ? request.getStatus() : CouponStatus.ACTIVE)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        if (request.getTargetItemIds() != null && !request.getTargetItemIds().isEmpty()) {
            List<Item> items = itemRepository.findAllById(request.getTargetItemIds());
            for (Item item : items) {
                // Verify item belongs to store
                if (!item.getStore().getId().equals(store.getId())) {
                    throw new CustomException(ErrorCode.BAD_REQUEST, "다른 가게의 상품이 포함되어 있습니다.");
                }
                CouponItem couponItem = CouponItem.builder()
                        .coupon(savedCoupon)
                        .item(item)
                        .build();
                couponItemRepository.save(couponItem);
            }
        }

        return savedCoupon.getId();
    }

    @Transactional
    public void updateCoupon(Long couponId, User user, UpdateCouponRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        validateStoreOwner(coupon.getStore(), user);

        coupon.updateCoupon(
                request.getTitle(),
                request.getDescription(),
                request.getIssueStartsAt(),
                request.getIssueEndsAt(),
                request.getTotalQuantity(),
                request.getLimitPerUser(),
                request.getStatus());
    }

    @Transactional
    public void deleteCoupon(Long couponId, User user) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        validateStoreOwner(coupon.getStore(), user);

        couponRepository.delete(coupon);
    }

    @Transactional
    public void verifyAndUseCoupon(Long storeId, User owner, String verificationCode) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, owner);

        // 검증 코드로 우리 가게 활성화 쿠폰 조회
        StudentCoupon studentCoupon = studentCouponRepository.findForOwnerVerification(
                storeId, verificationCode, CouponUsageStatus.ACTIVATED
        ).orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "유효하지 않은 코드이거나 활성화되지 않은 쿠폰입니다."));

        // 사용 처리
        studentCoupon.use();
    }

    // --- 공통 ---

    public List<CouponResponse> getCouponsByStore(Long storeId) {
        return couponRepository.findByStoreId(storeId).stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }

    public List<CouponResponse> getCouponsByItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        return couponItemRepository.findByItem(item).stream()
                .map(CouponItem::getCoupon)
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }

    // --- 학생용 ---

    @Transactional
    public IssueCouponResponse issueCoupon(Long couponId, User user) {
        Coupon coupon = couponRepository.findById(couponId)
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

        Integer userCount = studentCouponRepository.countByCouponAndUser(coupon, user);
        if (userCount >= coupon.getLimitPerUser()) {
            throw new CustomException(ErrorCode.UNPROCESSABLE_ENTITY, "인당 발급 한도를 초과했습니다.");
        }

        StudentCoupon studentCoupon = StudentCoupon.builder()
                .user(user)
                .coupon(coupon)
                .status(CouponUsageStatus.UNUSED)
                .expiresAt(now.plusDays(30))
                .build();

        studentCouponRepository.save(studentCoupon);

        return IssueCouponResponse.from(studentCoupon);
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

        return studentCoupon.activate();
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
}
