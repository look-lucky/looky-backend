package com.looky.domain.coupon.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(length = 4)
    private String verificationCode; // 점주 확인용 고유 코드 (쿠폰 사용 버튼 클릭 시 발급)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponUsageStatus status = CouponUsageStatus.UNUSED;

    private LocalDateTime issuedAt = LocalDateTime.now();

    private LocalDateTime activatedAt; // 사용 버튼 누른 시점 (타이머용)

    private LocalDateTime usedAt; // 사용 시점

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 쿠폰 만료 시점

    @Builder
    public StudentCoupon(User user, Coupon coupon, CouponUsageStatus status, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.user = user;
        this.coupon = coupon;
        this.status = status != null ? status : CouponUsageStatus.UNUSED;
        this.issuedAt = issuedAt != null ? issuedAt : LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public String activate() {
        if (this.status != CouponUsageStatus.UNUSED) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "이미 사용 중이거나 사용된 쿠폰입니다.");
        }

        if (LocalDateTime.now().isAfter(this.expiresAt)) {
            this.status = CouponUsageStatus.EXPIRED;
            throw new CustomException(ErrorCode.STATE_CONFLICT, "만료된 쿠폰입니다.");
        }

        this.verificationCode = generateRandomCode();
        this.status = CouponUsageStatus.ACTIVATED;
        this.activatedAt = LocalDateTime.now();

        return this.verificationCode;
    }

    public void use() {
        if (this.status != CouponUsageStatus.ACTIVATED) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "만료된 쿠폰입니다.");
        }
        this.status = CouponUsageStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    // 0000 ~ 9999 랜덤 생성
    private String generateRandomCode() {
        int number = ThreadLocalRandom.current().nextInt(0, 10000);
        return String.format("%04d", number);
    }
}
