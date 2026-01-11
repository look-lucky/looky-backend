package com.neardeal.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int orderIndex; 

    @Builder
    public ReviewImage(Review review, String imageUrl, int orderIndex) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
    }
}
