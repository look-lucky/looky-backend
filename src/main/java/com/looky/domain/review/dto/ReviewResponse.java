package com.looky.domain.review.dto;

import com.looky.domain.review.entity.Review;
import com.looky.domain.review.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long reviewId;
    private Long storeId;
    private String username;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
    private int likeCount;
    private List<String> imageUrls;
    private List<ReviewResponse> replies;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .storeId(review.getStore().getId())
                .username(review.getUser().getUsername())
                .content(review.getContent())
                .imageUrls(review.getImages().stream().map(ReviewImage::getImageUrl).toList())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .likeCount(review.getLikeCount())
                .replies(review.getReplies().stream().map(ReviewResponse::from).toList())
                .build();
    }
}
