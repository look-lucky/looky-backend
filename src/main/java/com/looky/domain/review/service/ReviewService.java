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

    // --- 점주용 ---

    @Transactional
    public Long createReplyForOwner(User user, Long storeId, Long parentReviewId, CreateReviewRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        if (!store.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 가게의 리뷰에만 답글을 달 수 있습니다.");
        }

        Review parentReview = reviewRepository.findById(parentReviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "원본 리뷰를 찾을 수 없습니다."));

        if (parentReview.getParentReview() != null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "답글에 답글을 달 수 없습니다.");
        }

        List<String> imageUrls = request.getImageUrls();
        if (imageUrls != null && imageUrls.size() > 3) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 3장까지 등록할 수 있습니다.");
        }

        Review review = Review.builder()
                .user(user)
                .store(store)
                .content(request.getContent())
                .rating(null)
                .isVerified(false)
                .parentReview(parentReview)
                .build();

        addReviewImages(review, imageUrls);
        reviewRepository.save(review);
        return review.getId();
    }

    @Transactional
    public void updateReviewForOwner(Long reviewId, User user, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        review.updateReview(
                request.getContent().orElse(review.getContent()),
                review.getRating(),
                review.getIsVerified()
        );

        syncReviewImages(review, request);
    }

    @Transactional
    public void deleteReviewForOwner(Long reviewId, User user) {
        deleteReviewInternal(reviewId, user);
    }

    public Page<OwnerReviewResponse> getReviewsForOwner(Long storeId, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        Page<Review> parentReviews = reviewRepository.findByStoreAndParentReviewIsNull(store, pageable);
        List<Review> parents = parentReviews.getContent();

        if (parents.isEmpty()) {
            return parentReviews.map(review ->
                    OwnerReviewResponse.from(review, resolveNickname(review, Collections.emptyMap(), store)));
        }

        List<Review> replies = reviewRepository.findByParentReviewIn(parents);

        Map<Long, List<Review>> repliesByParentId = replies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getParentReview().getId()));

        List<Review> allReviews = new ArrayList<>(parents);
        allReviews.addAll(replies);
        Map<Long, String> nicknameMap = buildNicknameMap(allReviews);

        return parentReviews.map(review -> {
            OwnerReviewResponse response = OwnerReviewResponse.from(review, resolveNickname(review, nicknameMap, store));

            List<Review> childReviews = repliesByParentId.getOrDefault(review.getId(), Collections.emptyList());
            response.setChildren(childReviews.stream()
                    .map(r -> OwnerReviewResponse.from(r, resolveNickname(r, nicknameMap, store)))
                    .toList());
            return response;
        });
    }

    public Page<OwnerReviewResponse> getMyReviewsForOwner(User user, Pageable pageable) {
        return reviewRepository.findByUserAndParentReviewIsNull(user, pageable)
                .map(review -> OwnerReviewResponse.from(review, review.getStore().getName() + " 사장님"));
    }

    public ReviewStatsResponse getReviewStatsForOwner(Long storeId) {
        return getReviewStatsInternal(storeId);
    }

    @Transactional
    public void reportReviewForOwner(Long reviewId, Long reporterId, ReportRequest request) {
        reportReviewInternal(reviewId, reporterId, request);
    }

    @Transactional
    public void addLikeForOwner(User user, Long reviewId) {
        addLikeInternal(user, reviewId);
    }

    @Transactional
    public void removeLikeForOwner(User user, Long reviewId) {
        removeLikeInternal(user, reviewId);
    }

    // --- 학생용 ---

    @Transactional
    public Long createReviewForStudent(User user, Long storeId, CreateReviewRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        Integer rating = request.getRating();
        if (rating == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "평점은 필수입니다.");
        }

        if (reviewRepository.existsByUserAndStoreAndParentReviewIsNull(user, store)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 해당 상점에 대한 리뷰를 작성했습니다.");
        }

        boolean isVerified = studentCouponRepository.existsByUserAndCoupon_StoreAndStatus(user, store, CouponUsageStatus.USED);

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
                .parentReview(null)
                .build();

        addReviewImages(review, imageUrls);
        reviewRepository.save(review);
        return review.getId();
    }

    @Transactional
    public Long createReplyForStudent(User user, Long storeId, Long parentReviewId, CreateReviewRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        Review parentReview = reviewRepository.findById(parentReviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "원본 리뷰를 찾을 수 없습니다."));

        if (parentReview.getParentReview() != null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "답글에 답글을 달 수 없습니다.");
        }

        List<String> imageUrls = request.getImageUrls();
        if (imageUrls != null && imageUrls.size() > 3) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 3장까지 등록할 수 있습니다.");
        }

        Review review = Review.builder()
                .user(user)
                .store(store)
                .content(request.getContent())
                .rating(null)
                .isVerified(false)
                .parentReview(parentReview)
                .build();

        addReviewImages(review, imageUrls);
        reviewRepository.save(review);
        return review.getId();
    }

    @Transactional
    public void updateReviewForStudent(Long reviewId, User user, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        boolean isVerified = studentCouponRepository.existsByUserAndCoupon_StoreAndStatus(user, review.getStore(), CouponUsageStatus.USED);

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

        syncReviewImages(review, request);
    }

    @Transactional
    public void deleteReviewForStudent(Long reviewId, User user) {
        deleteReviewInternal(reviewId, user);
    }

    public Page<StudentReviewResponse> getReviewsForStudent(Long storeId, Pageable pageable, User user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        Page<Review> parentReviews = reviewRepository.findByStoreAndParentReviewIsNull(store, pageable);
        List<Review> parents = parentReviews.getContent();

        if (parents.isEmpty()) {
            return parentReviews.map(review ->
                    StudentReviewResponse.from(review, false, resolveNickname(review, Collections.emptyMap(), store)));
        }

        List<Review> replies = reviewRepository.findByParentReviewIn(parents);

        Map<Long, List<Review>> repliesByParentId = replies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getParentReview().getId()));

        List<Review> allReviews = new ArrayList<>(parents);
        allReviews.addAll(replies);

        Set<Long> likedReviewIds = new HashSet<>();
        if (user != null && !allReviews.isEmpty()) {
            likedReviewIds = reviewLikeRepository.findByUserAndReviewIn(user, allReviews).stream()
                    .map(like -> like.getReview().getId())
                    .collect(Collectors.toSet());
        }

        final Set<Long> finalLikedReviewIds = likedReviewIds;
        Map<Long, String> nicknameMap = buildNicknameMap(allReviews);

        return parentReviews.map(review -> {
            StudentReviewResponse response = StudentReviewResponse.from(
                    review, finalLikedReviewIds.contains(review.getId()), resolveNickname(review, nicknameMap, store));

            List<Review> childReviews = repliesByParentId.getOrDefault(review.getId(), Collections.emptyList());
            response.setChildren(childReviews.stream()
                    .map(r -> StudentReviewResponse.from(
                            r, finalLikedReviewIds.contains(r.getId()), resolveNickname(r, nicknameMap, store)))
                    .toList());
            return response;
        });
    }

    public Page<StudentReviewResponse> getMyReviewsForStudent(User user, Pageable pageable) {
        String nickname = studentProfileRepository.findNicknameByUser(user).orElse("알 수 없음");
        return reviewRepository.findByUserAndParentReviewIsNull(user, pageable)
                .map(review -> StudentReviewResponse.from(review, false, nickname));
    }

    public ReviewStatsResponse getReviewStatsForStudent(Long storeId) {
        return getReviewStatsInternal(storeId);
    }

    @Transactional
    public void reportReviewForStudent(Long reviewId, Long reporterId, ReportRequest request) {
        reportReviewInternal(reviewId, reporterId, request);
    }

    @Transactional
    public void addLikeForStudent(User user, Long reviewId) {
        addLikeInternal(user, reviewId);
    }

    @Transactional
    public void removeLikeForStudent(User user, Long reviewId) {
        removeLikeInternal(user, reviewId);
    }

    // -- 내부 메서드 --

    private void deleteReviewInternal(Long reviewId, User user) {
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

    private void reportReviewInternal(Long reviewId, Long reporterId, ReportRequest request) {
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

    private void addLikeInternal(User user, Long reviewId) {
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

    private void removeLikeInternal(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 리뷰입니다."));

        if (!reviewLikeRepository.existsByUserAndReview(user, review)) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "좋아요를 누른 리뷰가 아닙니다.");
        }

        reviewLikeRepository.deleteByUserAndReview(user, review);
        review.decreaseLikeCount();
    }

    private ReviewStatsResponse getReviewStatsInternal(Long storeId) {
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

    private Map<Long, String> buildNicknameMap(List<Review> reviews) {
        List<Long> studentUserIds = reviews.stream()
                .filter(r -> r.getUser().getRole() == Role.ROLE_STUDENT)
                .map(r -> r.getUser().getId())
                .distinct()
                .toList();

        Map<Long, String> nicknameMap = new HashMap<>();
        if (!studentUserIds.isEmpty()) {
            studentProfileRepository.findAllById(studentUserIds)
                    .forEach(p -> nicknameMap.put(p.getUserId(), p.getNickname()));
        }
        return nicknameMap;
    }

    private String resolveNickname(Review review, Map<Long, String> nicknameMap, Store store) {
        if (review.getUser().getRole() == Role.ROLE_STUDENT) {
            return nicknameMap.getOrDefault(review.getUser().getId(), "알 수 없음");
        } else if (review.getUser().getRole() == Role.ROLE_OWNER) {
            return store.getName() + " 사장님";
        }
        return "알 수 없음";
    }

    private void addReviewImages(Review review, List<String> imageUrls) {
        if (imageUrls == null) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            review.addImage(ReviewImage.builder()
                    .review(review)
                    .imageUrl(imageUrls.get(i))
                    .orderIndex(i)
                    .build());
        }
    }

    private void syncReviewImages(Review review, UpdateReviewRequest request) {
        if (!request.getImageUrls().isPresent()) return;

        List<String> desiredUrls = request.getImageUrls().get() != null
                ? request.getImageUrls().get() : Collections.emptyList();

        if (desiredUrls.size() > 3) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 3장까지 등록할 수 있습니다.");
        }

        Set<String> desiredSet = new HashSet<>(desiredUrls);

        review.getImages().stream()
                .filter(img -> !desiredSet.contains(img.getImageUrl()))
                .toList()
                .forEach(img -> {
                    s3Service.deleteFile(img.getImageUrl());
                    review.removeImage(img);
                });

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

        Map<String, ReviewImage> urlToImage = review.getImages().stream()
                .collect(Collectors.toMap(ReviewImage::getImageUrl, img -> img));
        for (int i = 0; i < desiredUrls.size(); i++) {
            ReviewImage img = urlToImage.get(desiredUrls.get(i));
            if (img != null) img.updateOrderIndex(i);
        }
    }

    // todo: 삭제 예정

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
}
