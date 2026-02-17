package com.looky.domain.review.dto;

import com.looky.domain.review.entity.Review;
import com.looky.domain.review.entity.ReviewImage;
import com.looky.domain.user.entity.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long reviewId;
    private Long storeId;
    private String storeName;
    private Long parentReviewId;
    private String username;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
    private int likeCount;
    private boolean isOwnerReply;
    private boolean isLiked;
    private List<String> imageUrls;
    private List<ReviewResponse> children;

    public static ReviewResponse from(Review review, boolean isLiked) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .storeId(review.getStore().getId())
                .storeName(review.getStore().getName())
                .parentReviewId(review.getParentReview() != null ? review.getParentReview().getId() : null)
                .username(review.getUser().getUsername())
                .content(review.getContent())
                .imageUrls(review.getImages().stream().map(ReviewImage::getImageUrl).toList())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .likeCount(review.getLikeCount())
                .isOwnerReply(review.getParentReview() == null &&
                        review.getReplies().stream().anyMatch(r -> r.getUser().getRole() == Role.ROLE_OWNER))
                .isLiked(isLiked)
                .build();
    }
}
