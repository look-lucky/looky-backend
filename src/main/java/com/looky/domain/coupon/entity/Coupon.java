package com.looky.domain.coupon.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.store.entity.Store;
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

    private LocalDateTime issueStartsAt; // 쿠폰 노출/발급 시작일
    private LocalDateTime issueEndsAt; // 쿠폰 노출/발급 종료일

    private Integer totalQuantity; // 총 발행 한도 (null이면 무한대)

    @Column(nullable = false)
    private Integer limitPerUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponBenefitType benefitType; // 혜택 타입 (정액, 정률, 서비스)

    private String benefitValue; // 혜택 값 (할인 금액, 할인율, 서비스 내용 등)
    private Integer minOrderAmount; // 최소 주문 금액

    @Column(nullable = false)
    private Integer downloadCount = 0; // 현재 발급된 수량 (다운로드 수)

    @Builder
    public Coupon(Store store, String title, String description, LocalDateTime issueStartsAt, LocalDateTime issueEndsAt, Integer totalQuantity, Integer limitPerUser, CouponStatus status, CouponBenefitType benefitType, String benefitValue, Integer minOrderAmount) {
        this.store = store;
        this.title = title;
        this.description = description;
        this.issueStartsAt = issueStartsAt;
        this.issueEndsAt = issueEndsAt;
        this.totalQuantity = totalQuantity;
        this.limitPerUser = limitPerUser;
        this.status = status;
        this.benefitType = benefitType;
        this.benefitValue = benefitValue;
        this.minOrderAmount = minOrderAmount;
    }

    public void updateCoupon(String title, String description, LocalDateTime issueStartsAt, LocalDateTime issueEndsAt, Integer totalQuantity, Integer limitPerUser, CouponStatus status, CouponBenefitType benefitType, String benefitValue, Integer minOrderAmount) {
        this.title = title;
        this.description = description;
        this.issueStartsAt = issueStartsAt;
        this.issueEndsAt = issueEndsAt;
        this.totalQuantity = totalQuantity;
        this.limitPerUser = limitPerUser;
        this.status = status;
        this.benefitType = benefitType;
        this.benefitValue = benefitValue;
        this.minOrderAmount = minOrderAmount;
    }
}
