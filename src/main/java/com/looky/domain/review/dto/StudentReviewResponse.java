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
public class StudentReviewResponse {

    private Long reviewId;
    private Long storeId;
    private String storeName;
    private Long parentReviewId;
    private String username;
    private String nickname;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
    private int likeCount;
    private boolean isOwnerReply;
    private boolean isLiked;
    private List<String> imageUrls;
    private List<StudentReviewResponse> children;

    public static StudentReviewResponse from(Review review, boolean isLiked, String nickname) {
        return StudentReviewResponse.builder()
                .reviewId(review.getId())
                .storeId(review.getStore().getId())
                .storeName(review.getStore().getName())
                .parentReviewId(review.getParentReview() != null ? review.getParentReview().getId() : null)
                .username(review.getUser().getUsername())
                .nickname(nickname)
                .content(review.getContent())
                .imageUrls(review.getImages().stream().map(ReviewImage::getImageUrl).toList())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .likeCount(review.getLikeCount())
                .isOwnerReply(review.getParentReview() == null && review.getReplies().stream().anyMatch(r -> r.getUser().getRole() == Role.ROLE_OWNER))
                .isLiked(isLiked)
                .build();
    }
}
