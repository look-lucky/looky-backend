package com.looky.domain.coupon.repository;

import com.looky.domain.coupon.entity.CouponUsageStatus;
import com.looky.domain.coupon.entity.StudentCoupon;
import com.looky.domain.store.entity.Store;
import com.looky.domain.user.entity.User;
import com.looky.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudentCouponRepository extends JpaRepository<StudentCoupon, Long> {
    Integer countByCouponAndUser(Coupon coupon, User user);

    @Query("SELECT sc FROM StudentCoupon sc " +
            "JOIN FETCH sc.coupon c " +
            "JOIN FETCH c.store " +
            "WHERE sc.user = :user")
    List<StudentCoupon> findByUser(@Param("user") User user);

    Optional<StudentCoupon> findByIdAndUser(Long id, User user);

    // 특정 가게 총 사용된 쿠폰 수
    long countByCoupon_StoreIdAndStatus(Long storeId, CouponUsageStatus status);

    // 검증 코드로 우리 가게 쿠폰 조회 (쿠폰 사용 처리용)
    @Query("SELECT cc FROM StudentCoupon cc " +
            "JOIN cc.coupon c " +
            "WHERE c.store.id = :storeId " +
            "AND cc.verificationCode = :code " +
            "AND cc.status = :status")
    Optional<StudentCoupon> findForOwnerVerification(
            @Param("storeId") Long storeId,
            @Param("code") String code,
            @Param("status") CouponUsageStatus status
    );

    // 해당 유저가 특정 상점에서 쿠폰을 사용한 적이 있는가? (리뷰 검증용)
    boolean existsByUserAndCoupon_StoreAndStatus(User user, Store store, CouponUsageStatus status);

    List<StudentCoupon> findByUserAndCouponIn(User user, List<Coupon> coupons);

    // 쿠폰 사용 횟수 조회 (점주용)
    @Query("SELECT sc.coupon.id, COUNT(sc) FROM StudentCoupon sc WHERE sc.coupon IN :coupons AND sc.status = :status GROUP BY sc.coupon.id")
    List<Object[]> countByCouponInAndStatus(@Param("coupons") List<Coupon> coupons, @Param("status") CouponUsageStatus status);

    // 활성화된지 30분 지난 쿠폰 다시 UNUSED 상태로 되돌리기
    @Modifying(clearAutomatically = true) // 벌크 연산 후 영속성 컨텍스트를 비워 데이터 불일치 방지
    @Query("UPDATE StudentCoupon c " +
            "SET c.status = 'UNUSED', " +        // 상태를 다시 '사용 안 함'으로 변경
            "    c.verificationCode = NULL, " +  // 인증 코드 삭제
            "    c.activatedAt = NULL " +        // 활성화 시간 초기화
            "WHERE c.status = 'ACTIVATED' " +    // 현재 '활성화' 상태이고
            "  AND c.activatedAt < :threshold")  // 지정된 시간(30분)이 지난 경우
    int resetExpiredCoupons(@Param("threshold") LocalDateTime threshold);
}
