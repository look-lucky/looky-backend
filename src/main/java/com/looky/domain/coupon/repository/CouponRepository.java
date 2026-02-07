package com.looky.domain.coupon.repository;

import com.looky.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByStoreId(Long storeId);

    long countByStoreId(Long storeId);

    @Query("SELECT DISTINCT c FROM Coupon c " +
           "JOIN c.store s " +
           "JOIN Partnership p ON p.store = s " +
           "JOIN p.organization o " +
           "WHERE o.university.id = :universityId " +
           "AND c.issueStartsAt BETWEEN :startOfDay AND :endOfDay " +
           "AND p.startsAt <= :today AND p.endsAt >= :today " +
           "ORDER BY c.issueStartsAt DESC")
    List<Coupon> findTodayCouponsByUniversity(
            @Param("universityId") Long universityId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("today") LocalDate today
    );

    @Query("SELECT c FROM Coupon c " +
           "WHERE c.store.id IN :storeIds " +
           "AND c.issueStartsAt <= :now AND c.issueEndsAt >= :now " +
           "AND c.status = 'ACTIVE'")
    List<Coupon> findActiveCouponsByStoreIds(
            @Param("storeIds") List<Long> storeIds,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(c) > 0 FROM Coupon c " +
           "WHERE c.store.id = :storeId " +
           "AND c.issueStartsAt <= :now AND c.issueEndsAt >= :now " +
           "AND c.status = 'ACTIVE'")
    boolean existsActiveCoupon(
            @Param("storeId") Long storeId,
            @Param("now") LocalDateTime now
    );
}
