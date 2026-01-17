package com.neardeal.domain.review.service;

import com.neardeal.common.exception.CustomException;
import com.neardeal.common.exception.ErrorCode;
import com.neardeal.domain.coupon.entity.CouponUsageStatus;
import com.neardeal.domain.coupon.repository.CustomerCouponRepository;
import com.neardeal.domain.review.dto.*;
import com.neardeal.domain.review.entity.Review;
import com.neardeal.domain.review.entity.ReviewReport;
import com.neardeal.domain.review.repository.ReviewReportRepository;
import com.neardeal.domain.review.repository.ReviewRepository;
import com.neardeal.domain.store.entity.Store;
import com.neardeal.domain.store.repository.StoreRepository;
import com.neardeal.domain.user.entity.User;
import com.neardeal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final CustomerCouponRepository customerCouponRepository;
    private final UserRepository userRepository;
    private final ReviewReportRepository reviewReportRepository;

    @Transactional
    public Long createReview(User user, Long storeId, CreateReviewRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        if (reviewRepository.existsByUserAndStore(user, store)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 해당 상점에 대한 리뷰를 작성했습니다.");
        }

        boolean isVerified = customerCouponRepository.existsByUserAndCoupon_StoreAndStatus(user, store, CouponUsageStatus.USED);

        Review review = Review.builder()
                .user(user)
                .store(store)
                .content(request.getContent())
                .rating(request.getRating())
                .isVerified(isVerified)
                .parentReview(null)
                .build();

        reviewRepository.save(review);
        return review.getId();
    }

    @Transactional
    public void updateReview(Long reviewId, User user, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."));


        if (!review.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Store store = review.getStore();
        boolean isVerified = customerCouponRepository.existsByUserAndCoupon_StoreAndStatus(user, store, CouponUsageStatus.USED);

        review.updateReview(request.getContent(), request.getRating(), isVerified);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        reviewRepository.delete(review);
    }

    public Page<ReviewResponse> getReviews(Long storeId, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        return reviewRepository.findByStore(store, pageable)
                .map(ReviewResponse::from);
    }

    public ReviewStatsResponse getReviewStats(Long storeId) {
        Double avgRating = reviewRepository.findAverageRatingByStoreId(storeId);
        Long totalReviews = reviewRepository.countByStoreId(storeId);
        Long rating1 = reviewRepository.countByStoreIdAndRating(storeId, 1);
        Long rating2 = reviewRepository.countByStoreIdAndRating(storeId, 2);
        Long rating3 = reviewRepository.countByStoreIdAndRating(storeId, 3);
        Long rating4 = reviewRepository.countByStoreIdAndRating(storeId, 4);
        Long rating5 = reviewRepository.countByStoreIdAndRating(storeId, 5);

        return ReviewStatsResponse.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .rating1Count(rating1 != null ? rating1 : 0L)
                .rating2Count(rating2 != null ? rating2 : 0L)
                .rating3Count(rating3 != null ? rating3 : 0L)
                .rating4Count(rating4 != null ? rating4 : 0L)
                .rating5Count(rating5 != null ? rating5 : 0L)
                .build();
    }

    // 리뷰 신고
    @Transactional
    public void reportReview(Long reviewId, Long reporterId, ReportRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."));

        User reporter = userRepository.getReferenceById(reporterId);

        if (reviewReportRepository.existsByReviewAndReporter(review, reporter)) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "이미 신고한 리뷰입니다.");
        }

        ReviewReport report = new ReviewReport(review, reporter, request.getReason(), request.getDetail());
        reviewReportRepository.save(report);

        review.increaseReportCount();
    }
}
