
package com.looky.domain.review.service;

import com.looky.common.util.FileValidator;

import com.looky.common.service.S3Service;
import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.coupon.entity.CouponUsageStatus;
import com.looky.domain.coupon.repository.StudentCouponRepository;
import com.looky.domain.review.dto.*;
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
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final StoreRepository storeRepository;
    private final StudentCouponRepository studentCouponRepository;
    private final UserRepository userRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createReview(User user, Long storeId, CreateReviewRequest request, List<MultipartFile> images) throws IOException {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        Review parentReview = null;
        boolean isVerified = false;
        Integer rating = request.getRating();

        // 답글 작성인 경우
        if (request.getParentReviewId() != null) {
            parentReview = reviewRepository.findById(request.getParentReviewId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "원본 리뷰를 찾을 수 없습니다."));

            // 답글의 답글 불가 (Depth 1 제한)
            if (parentReview.getParentReview() != null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "답글에 답글을 달 수 없습니다.");
            }
            
            // 점주인 경우
            if (user.getRole() == Role.ROLE_OWNER) {
                // 본인 가게 리뷰에만 답글 가능
                if (!store.getUser().getId().equals(user.getId())) {
                     throw new CustomException(ErrorCode.FORBIDDEN, "본인 가게의 리뷰에만 답글을 달 수 있습니다.");
                }
                // 답글은 평점 없음
                rating = null;
            } 
            // 학생인 경우
            else if (user.getRole() == Role.ROLE_STUDENT) {
                 // 답글은 평점 없음
                 rating = null;
            } else {
                 throw new CustomException(ErrorCode.FORBIDDEN);
            }

        } 
        // 일반 리뷰 작성인 경우
        else {
            // 점주는 일반 리뷰 작성 불가
            if (user.getRole() == Role.ROLE_OWNER) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "점주는 답글만 가능합니다.");
            }

            if (rating == null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "평점은 필수입니다.");
            }

            // 학생: 중복 리뷰 확인 (일반 리뷰는 상점당 1개 제한 유지)
            if (reviewRepository.existsByUserAndStoreAndParentReviewIsNull(user, store)) {
                throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 해당 상점에 대한 리뷰를 작성했습니다.");
            }
            
            // 학생: 쿠폰 사용 여부로 인증 확인
            isVerified = studentCouponRepository.existsByUserAndCoupon_StoreAndStatus(user, store,
                    CouponUsageStatus.USED);
        }

        Review review = Review.builder()
                .user(user)
                .store(store)
                .content(request.getContent())
                .rating(rating)
                .isVerified(isVerified)
                .parentReview(parentReview)
                .build();

        // 이미지 유효성 검사 (최대 3장, 10MB)
        FileValidator.validateImageFiles(images, 3, 10 * 1024 * 1024);

        // 이미지 S3 업로드 및 저장
        uploadAndSaveImages(review, images);

        reviewRepository.save(review);
        return review.getId();
    }

    @Transactional
    public void updateReview(Long reviewId, User user, UpdateReviewRequest request, List<MultipartFile> images)
            throws IOException {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 학생 리뷰인 경우 인증 상태 업데이트 체크
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

        // 새 이미지가 존재하면 기존 것 모두 삭제 후 새로 등록
        if (images != null && !images.isEmpty()) {

            // S3 파일 삭제
            for (ReviewImage oldImage : review.getImages()) {
                s3Service.deleteFile(oldImage.getImageUrl());
            }

            // DB 삭제 (orphanRemoval = true로 인해 리스트에서 제거하면 삭제됨)
            review.getImages().clear();

            // 이미지 유효성 검사 (최대 3장, 10MB)
            FileValidator.validateImageFiles(images, 3, 10 * 1024 * 1024);

            // 새 이미지 업로드
            uploadAndSaveImages(review, images);
        }
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // S3 이미지 삭제
        for (ReviewImage image : review.getImages()) {
            s3Service.deleteFile(image.getImageUrl());
        }

        reviewRepository.delete(review);
    }

    // 특정 상점 리뷰 목록 조회
    public Page<ReviewResponse> getReviews(Long storeId, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        // 1. 부모 리뷰만 페이징 조회
        Page<Review> parentReviews = reviewRepository.findByStoreAndParentReviewIsNull(store, pageable);

        // 2. 조회된 부모 리뷰들의 ID 수집
        List<Review> parents = parentReviews.getContent();
        if (parents.isEmpty()) {
            return parentReviews.map(ReviewResponse::from);
        }

        // 3. 부모 리뷰들에 대한 답글 일괄 조회
        List<Review> replies = reviewRepository.findByParentReviewIn(parents);

        // 4. 답글을 부모 리뷰 ID로 그룹화
        java.util.Map<Long, List<Review>> repliesByParentId = replies.stream()
                .collect(java.util.stream.Collectors.groupingBy(reply -> reply.getParentReview().getId()));

        // 5. ReviewResponse로 변환 및 답글 매핑
        return parentReviews.map(review -> {
            ReviewResponse response = ReviewResponse.from(review);
            List<Review> childReviews = repliesByParentId.getOrDefault(review.getId(), java.util.Collections.emptyList());
            response.setChildren(childReviews.stream()
                    .map(ReviewResponse::from)
                    .toList());
            return response;
        });
    }

    // 내가 작성한 리뷰 목록 조회
    public Page<ReviewResponse> getMyReviews(User user, Pageable pageable) {
        return reviewRepository.findByUserAndParentReviewIsNull(user, pageable)
                .map(ReviewResponse::from);
    }

    // 특정 상점 리뷰 통계 조회
    public ReviewStatsResponse getReviewStats(Long storeId) {
        // 평점 및 개수 산정 시 답글 제외
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

    // 리뷰 좋아요
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

    // 리뷰 좋아요 취소
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

    private void uploadAndSaveImages(Review review, List<MultipartFile> images) throws IOException {
        if (images == null || images.isEmpty()) {
            return;
        }

        int currentOrderIndex = review.getImages().size();

        for (MultipartFile file : images) {
            if (file.isEmpty())
                continue;

            String imageUrl = s3Service.uploadFile(file);

            ReviewImage reviewImage = ReviewImage.builder()
                    .review(review)
                    .imageUrl(imageUrl)
                    .orderIndex(currentOrderIndex++)
                    .build();
            review.addImage(reviewImage);
        }
    }
}
