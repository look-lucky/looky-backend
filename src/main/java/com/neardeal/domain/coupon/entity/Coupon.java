package com.neardeal.domain.coupon.entity;

import com.neardeal.common.entity.BaseEntity;
import com.neardeal.domain.affiliation.entity.Affiliation;
import com.neardeal.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_affiliation_id")
    private Affiliation targetAffiliation; // 제휴 쿠폰일 경우 타겟 소속

    private LocalDateTime issueStartsAt; // 예약 발행 시 사용
    private LocalDateTime issueEndsAt; // 쿠폰 노출/발급 종료일

    @Column(nullable = false)
    private Integer totalQuantity; // 총 발행 한도

    @Column(nullable = false)
    private Integer limitPerUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    @Builder
    public Coupon(Store store, String title, String description, Affiliation targetAffiliation, LocalDateTime issueStartsAt, LocalDateTime issueEndsAt, Integer totalQuantity, Integer limitPerUser, CouponStatus status) {
        this.store = store;
        this.title = title;
        this.description = description;
        this.targetAffiliation = targetAffiliation;
        this.issueStartsAt = issueStartsAt;
        this.issueEndsAt = issueEndsAt;
        this.totalQuantity = totalQuantity;
        this.limitPerUser = limitPerUser;
        this.status = status;
    }

    public void updateCoupon(String title, String description, LocalDateTime issueStartsAt, LocalDateTime issueEndsAt,
            Integer totalQuantity, Integer limitPerUser, CouponStatus status) {
        if (title != null)
            this.title = title;
        if (description != null)
            this.description = description;
        if (issueStartsAt != null)
            this.issueStartsAt = issueStartsAt;
        if (issueEndsAt != null)
            this.issueEndsAt = issueEndsAt;
        if (totalQuantity != null)
            this.totalQuantity = totalQuantity;
        if (limitPerUser != null)
            this.limitPerUser = limitPerUser;
        if (status != null)
            this.status = status;
    }
}
