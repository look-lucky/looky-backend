package com.neardeal.domain.review.entity;

import com.neardeal.common.entity.BaseEntity;
import com.neardeal.domain.store.entity.Store;
import com.neardeal.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_review_id")
    private Review parentReview; // 답글인 경우 원본 리뷰 ID

    @Column(nullable = false)
    private boolean isVerified; // 구매인증여부 (쿠폰사용여부로 판단)

    @Column(nullable = false)
    private int rating;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @Column(nullable = false)
    private int reportCount;

    private boolean isPrivate;

    @Column(nullable = false)
    private int likeCount;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    @Builder
    public Review(User user, Store store, Review parentReview, boolean isVerified, int rating, String content) {
        this.user = user;
        this.store = store;
        this.parentReview = parentReview;
        this.isVerified = isVerified; // 쿠폰을 사용한 학생이 작성한 리뷰인지
        this.rating = rating;
        this.content = content;
        this.status = ReviewStatus.PUBLISHED; // 리뷰 첫 생성 시 바로 등록
        this.reportCount = 0; // 리뷰 첫 생성 시 신고 수는 0
    }

    public void updateReview(String content, Integer rating, Boolean isVerified) {
        if (content != null) {
            this.content = content;
        }
        if (rating != null) {
            this.rating = rating;
        }
        if (isVerified != null) {
            this.isVerified = isVerified;
        }
    }

    // 리뷰 신고 접수
    public void increaseReportCount() {
        this.reportCount++;

        // 신고가 10건 이상 누적되면 REPORTED 상태로 변경
        if (this.reportCount >= 10 && this.status == ReviewStatus.PUBLISHED) {
            this.status = ReviewStatus.REPORTED;
        }
    }

    // 리뷰 비활성화 (관리자용)
    public void banByAdmin() {
        this.status = ReviewStatus.BANNED;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void addImage(ReviewImage image) {
        this.images.add(image);
    }

    public void removeImage(ReviewImage image) {
        this.images.remove(image);
    }

}
