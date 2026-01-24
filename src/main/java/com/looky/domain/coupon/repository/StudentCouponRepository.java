package com.looky.domain.coupon.repository;

import com.looky.domain.coupon.entity.CouponUsageStatus;
import com.looky.domain.coupon.entity.StudentCoupon;
import com.looky.domain.store.entity.Store;
import com.looky.domain.user.entity.User;
import com.looky.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentCouponRepository extends JpaRepository<StudentCoupon, Long> {
    Integer countByCouponAndUser(Coupon coupon, User user);

    List<StudentCoupon> findByUser(User user);

    Optional<StudentCoupon> findByIdAndUser(Long id, User user);
    
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

}
