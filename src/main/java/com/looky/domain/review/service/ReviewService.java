package com.looky.domain.review.service;

import com.looky.common.service.S3Service;
import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.coupon.entity.CouponUsageStatus;
import com.looky.domain.coupon.repository.StudentCouponRepository;
import com.looky.domain.user.repository.StudentProfileRepository;
import com.looky.domain.review.dto.*;

import java.util.*;

import com.looky.domain.review.entity.Review;
import com.looky.domain.review.entity.ReviewImage;
import com.looky.domain.review.entity.ReviewLike;
import com.looky.domain.review.entity.ReviewReport;
import com.looky.domain.review.repository.ReviewLikeRepository;
import com.looky.domain.review.repository.ReviewReportRepository;
import com.looky.domain.review.repository.ReviewRepository;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final StoreRepository storeRepository;
    private final StudentCouponRepository studentCouponRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createReview(User user, Long storeId, CreateReviewRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        Review parentReview = null;
        boolean isVerified = false;
        Integer rating = request.getRating();

        if (request.getParentReviewId() != null) {
            parentReview = reviewRepository.findById(request.getParentReviewId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "원본 리뷰를 찾을 수 없습니다."));

            if (parentReview.getParentReview() != null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "답글에 답글을 달 수 없습니다.");
            }

            if (user.getRole() == Role.ROLE_OWNER) {
                if (!store.getUser().getId().equals(user.getId())) {
                    throw new CustomException(ErrorCode.FORBIDDEN, "본인 가게의 리뷰에만 답글을 달 수 있습니다.");
                }
                rating = null;
            } else if (user.getRole() == Role.ROLE_STUDENT) {
                rating = null;
            } else {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        } else {
            if (user.getRole() == Role.ROLE_OWNER) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "점주는 답글만 가능합니다.");
            }

            if (rating == null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "평점은 필수입니다.");
            }

            if (reviewRepository.existsByUserAndStoreAndParentReviewIsNull(user, store)) {
                throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 해당 상점에 대한 리뷰를 작성했습니다.");
            }

            isVerified = studentCouponRepository.existsByUserAndCoupon_StoreAndStatus(user, store, CouponUsageStatus.USED);
        }

        List<String> imageUrls = request.getImageUrls();
        if (imageUrls != null && imageUrls.size() > 3) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 3장까지 등록할 수 있습니다.");
        }

        Review review = Review.builder()
                .user(user)
                .store(store)
                .content(request.getContent())
                .rating(rating)
                .isVerified(isVerified)
                .parentReview(parentReview)
                .build();

        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                review.addImage(ReviewImage.builder()
                        .review(review)
                        .imageUrl(imageUrls.get(i))
                        .orderIndex(i)
                        .build());
            }
        }

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

        Boolean isVerified = review.getIsVerified();
        Store store = review.getStore();

        if (user.getRole() == Role.ROLE_STUDENT) {
            isVerified = studentCouponRepository.existsByUserAndCoupon_StoreAndStatus(user, store, CouponUsageStatus.USED);
        }

        if (request.getRating().isPresent()) {
            Integer rating = request.getRating().get();
            if (rating != null && (rating < 1 || rating > 5)) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "평점은 1점 이상 5점 이하여야 합니다.");
            }
        }

        review.updateReview(
                request.getContent().orElse(review.getContent()),
                request.getRating().orElse(review.getRating()),
                isVerified
        );

        // 이미지 처리
        if (request.getImageUrls().isPresent()) {
            List<String> desiredUrls = request.getImageUrls().get() != null
                    ? request.getImageUrls().get() : Collections.emptyList();

            if (desiredUrls.size() > 3) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 3장까지 등록할 수 있습니다.");
            }

            Set<String> desiredSet = new HashSet<>(desiredUrls);

            // desired에 없는 기존 이미지 삭제
            review.getImages().stream()
                    .filter(img -> !desiredSet.contains(img.getImageUrl()))
                    .toList()
                    .forEach(img -> {
                        s3Service.deleteFile(img.getImageUrl());
                        review.removeImage(img);
                    });

            // DB에 없는 새 URL 추가
            Set<String> existingUrls = review.getImages().stream()
                    .map(ReviewImage::getImageUrl)
                    .collect(Collectors.toSet());
            for (String url : desiredUrls) {
                if (!existingUrls.contains(url)) {
                    review.addImage(ReviewImage.builder()
                            .review(review)
                            .imageUrl(url)
                            .orderIndex(0)
                            .build());
                }
            }

            // desiredUrls 순서대로 인덱스 재정렬
            Map<String, ReviewImage> urlToImage = review.getImages().stream()
                    .collect(Collectors.toMap(ReviewImage::getImageUrl, img -> img));
            for (int i = 0; i < desiredUrls.size(); i++) {
                ReviewImage img = urlToImage.get(desiredUrls.get(i));
                if (img != null) img.updateOrderIndex(i);
            }
        }
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        for (ReviewImage image : review.getImages()) {
            s3Service.deleteFile(image.getImageUrl());
        }

        reviewRepository.delete(review);
    }

    public Page<ReviewResponse> getReviews(Long storeId, Pageable pageable, User user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        Page<Review> parentReviews = reviewRepository.findByStoreAndParentReviewIsNull(store, pageable);

        List<Review> parents = parentReviews.getContent();
        if (parents.isEmpty()) {
            return parentReviews.map(review -> {
                String fallbackNickname = "알 수 없음";
                if (review.getUser().getRole() == Role.ROLE_STUDENT) {
                    fallbackNickname = studentProfileRepository.findNicknameByUser(review.getUser()).orElse("알 수 없음");
                } else if (review.getUser().getRole() == Role.ROLE_OWNER) {
                    fallbackNickname = store.getName() + " 사장님";
                }
                return ReviewResponse.from(review, false, fallbackNickname);
            });
        }

        List<Review> replies = reviewRepository.findByParentReviewIn(parents);

        Map<Long, List<Review>> repliesByParentId = replies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getParentReview().getId()));

        Set<Long> likedReviewIds = new HashSet<>();
        List<Review> allReviews = new ArrayList<>(parents);
        allReviews.addAll(replies);

        if (user != null && !allReviews.isEmpty()) {
            likedReviewIds = reviewLikeRepository.findByUserAndReviewIn(user, allReviews).stream()
                    .map(like -> like.getReview().getId())
                    .collect(Collectors.toSet());
        }

        final Set<Long> finalLikedReviewIds = likedReviewIds;

        List<Long> studentUserIds = allReviews.stream()
                .filter(r -> r.getUser().getRole() == Role.ROLE_STUDENT)
                .map(r -> r.getUser().getId())
                .distinct()
                .toList();

        Map<Long, String> nicknameMap = new java.util.HashMap<>();

        if (!studentUserIds.isEmpty()) {
            List<com.looky.domain.user.entity.StudentProfile> students = studentProfileRepository.findAllById(studentUserIds);
            students.forEach(p -> nicknameMap.put(p.getUserId(), p.getNickname()));
        }

        return parentReviews.map(review -> {
            boolean isLiked = finalLikedReviewIds.contains(review.getId());
            String nickname = "알 수 없음";
            if (review.getUser().getRole() == Role.ROLE_STUDENT) {
                nickname = nicknameMap.getOrDefault(review.getUser().getId(), "알 수 없음");
            } else if (review.getUser().getRole() == Role.ROLE_OWNER) {
                nickname = store.getName() + " 사장님";
            }

            ReviewResponse response = ReviewResponse.from(review, isLiked, nickname);

            List<Review> childReviews = repliesByParentId.getOrDefault(review.getId(), java.util.Collections.emptyList());
            response.setChildren(childReviews.stream()
                    .map(r -> {
                        String replyNickname = "알 수 없음";
                        if (r.getUser().getRole() == Role.ROLE_STUDENT) {
                            replyNickname = nicknameMap.getOrDefault(r.getUser().getId(), "알 수 없음");
                        } else if (r.getUser().getRole() == Role.ROLE_OWNER) {
                            replyNickname = store.getName() + " 사장님";
                        }
                        return ReviewResponse.from(r, finalLikedReviewIds.contains(r.getId()), replyNickname);
                    })
                    .toList());
            return response;
        });
    }

    public Page<ReviewResponse> getMyReviews(User user, Pageable pageable) {
        String nickname = "알 수 없음";
        if (user.getRole() == Role.ROLE_STUDENT) {
            nickname = studentProfileRepository.findNicknameByUser(user).orElse("알 수 없음");
        }
        final String finalNickname = nickname;

        return reviewRepository.findByUserAndParentReviewIsNull(user, pageable)
                .map(review -> {
                    String appliedNickname = finalNickname;
                    if (user.getRole() == Role.ROLE_OWNER) {
                        appliedNickname = review.getStore().getName() + " 사장님";
                    }
                    return ReviewResponse.from(review, false, appliedNickname);
                });
    }

    public ReviewStatsResponse getReviewStats(Long storeId) {
        Double avgRating = reviewRepository.findAverageRatingByStoreId(storeId);
        Long totalReviews = reviewRepository.countByStoreIdAndParentReviewIsNull(storeId);

        Long rating1 = reviewRepository.countByStoreIdAndRatingAndParentReviewIsNull(storeId, 1);
        Long rating2 = reviewRepository.countByStoreIdAndRatingAndParentReviewIsNull(storeId, 2);
        Long rating3 = reviewRepository.countByStoreIdAndRatingAndParentReviewIsNull(storeId, 3);
        Long rating4 = reviewRepository.countByStoreIdAndRatingAndParentReviewIsNull(storeId, 4);
        Long rating5 = reviewRepository.countByStoreIdAndRatingAndParentReviewIsNull(storeId, 5);

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

    @Transactional
    public void addLike(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 리뷰입니다."));

        if (review.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "자신의 리뷰에는 좋아요를 누를 수 없습니다.");
        }

        if (reviewLikeRepository.existsByUserAndReview(user, review)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 좋아요를 누른 리뷰입니다.");
        }

        ReviewLike reviewLike = ReviewLike.builder()
                .user(user)
                .review(review)
                .build();

        reviewLikeRepository.save(reviewLike);
        review.increaseLikeCount();
    }

    @Transactional
    public void removeLike(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 리뷰입니다."));

        if (!reviewLikeRepository.existsByUserAndReview(user, review)) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "좋아요를 누른 리뷰가 아닙니다.");
        }

        reviewLikeRepository.deleteByUserAndReview(user, review);
        review.decreaseLikeCount();
    }
}
