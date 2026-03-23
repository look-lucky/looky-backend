package com.looky.domain.user.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.S3Service;
import com.looky.domain.coupon.entity.Coupon;
import com.looky.domain.coupon.entity.CouponStatus;
import com.looky.domain.coupon.repository.CouponRepository;
import com.looky.domain.item.repository.ItemCategoryRepository;
import com.looky.domain.item.repository.ItemRepository;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreImage;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.storenews.entity.StoreNews;
import com.looky.domain.storenews.entity.StoreNewsImage;
import com.looky.domain.storenews.repository.StoreNewsRepository;
import com.looky.domain.user.dto.ChangePasswordRequest;
import com.looky.domain.user.dto.ChangeUsernameRequest;
import com.looky.domain.user.dto.WithdrawRequest;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.entity.WithdrawalFeedback;
import com.looky.domain.user.entity.WithdrawalReason;
import com.looky.domain.user.repository.UserRepository;
import com.looky.domain.user.repository.WithdrawalFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WithdrawalFeedbackRepository withdrawalFeedbackRepository;
    private final RefreshTokenService refreshTokenService;
    private final StoreRepository storeRepository;
    private final CouponRepository couponRepository;
    private final StoreNewsRepository storeNewsRepository;
    private final ItemRepository itemRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final S3Service s3Service;

    // 아이디 변경
    @Transactional
    public void changeUsername(Long userId, ChangeUsernameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (userRepository.existsByUsername(request.getNewUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 아이디입니다.");
        }

        user.updateUsername(request.getNewUsername());
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이전 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(User user, WithdrawRequest request) {
        if (request.getReasons().contains(WithdrawalReason.OTHER)) {
            if (request.getDetailReason() == null || request.getDetailReason().trim().isEmpty()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "기타 사유 선택 시 상세 내용은 필수입니다.");
            }
        }

        WithdrawalFeedback feedback = WithdrawalFeedback.builder()
                .user(user)
                .reasons(request.getReasons())
                .detailReason(request.getDetailReason())
                .build();
        withdrawalFeedbackRepository.save(feedback);

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (currentUser.getRole() == Role.ROLE_OWNER) {
            cleanupOwnerResources(currentUser);
        }

        currentUser.withdraw();
        refreshTokenService.delete(currentUser.getId());
    }

    private void cleanupOwnerResources(User owner) {
        List<Store> stores = storeRepository.findAllByUser(owner);
        for (Store store : stores) {
            for (StoreImage image : store.getImages()) {
                s3Service.deleteFile(image.getImageUrl());
            }

            List<StoreNews> newsList = storeNewsRepository.findByStoreId(store.getId(), Pageable.unpaged()).getContent();
            for (StoreNews news : newsList) {
                for (StoreNewsImage image : news.getImages()) {
                    s3Service.deleteFile(image.getImageUrl());
                }
            }

            List<com.looky.domain.item.entity.Item> items = itemRepository.findByStoreId(store.getId());
            for (com.looky.domain.item.entity.Item item : items) {
                if (item.getImageUrl() != null) {
                    s3Service.deleteFile(item.getImageUrl());
                }
            }

            store.unclaim();

            List<Coupon> coupons = couponRepository.findByStoreId(store.getId());
            for (Coupon coupon : coupons) {
                if (coupon.getStatus() == CouponStatus.ACTIVE) {
                    coupon.expireByWithdrawal();
                }
            }

            storeNewsRepository.deleteByStore(store);
            itemRepository.deleteByStoreId(store.getId());
            itemCategoryRepository.deleteByStoreId(store.getId());
        }
    }
}
