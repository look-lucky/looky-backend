package com.neardeal.domain.coupon.repository;

import com.neardeal.domain.coupon.entity.CouponUsageStatus;
import com.neardeal.domain.coupon.entity.CustomerCoupon;
import com.neardeal.domain.user.entity.User;
import com.neardeal.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, Long> {
    Integer countByCouponAndUser(Coupon coupon, User user);

    List<CustomerCoupon> findByUser(User user);

    Optional<CustomerCoupon> findByIdAndUser(Long id, User user);
    
    // 검증 코드로 우리 가게 쿠폰 조회 (쿠폰 사용 처리용)
    @Query("SELECT cc FROM CustomerCoupon cc " +
            "JOIN cc.coupon c " +
            "WHERE c.store.id = :storeId " +
            "AND cc.verificationCode = :code " +
            "AND cc.status = :status")
    Optional<CustomerCoupon> findForOwnerVerification(
            @Param("storeId") Long storeId,
            @Param("code") String code,
            @Param("status") CouponUsageStatus status
    );

}
