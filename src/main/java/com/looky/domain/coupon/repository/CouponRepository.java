package com.looky.domain.coupon.repository;

import com.looky.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithLock(@Param("id") Long id);
    
    // 특정 상점이 발행한 모든 쿠폰 목록 조회
    List<Coupon> findByStoreId(Long storeId);

    // 특정 상점이 발행한 쿠폰의 총 개수 조회
    long countByStoreId(Long storeId);

    // 사용자의 학교에 속한 상점에서 오늘 발급 시작된 쿠폰 목록 조회
    @Query("SELECT DISTINCT c FROM Coupon c " +
           "JOIN c.store s " +
           "JOIN StoreUniversity su ON su.store = s " +
           "WHERE su.university.id = :universityId " +
           "AND c.issueStartsAt <= :now AND c.issueStartsAt >= :since " +
           "ORDER BY c.issueStartsAt DESC")
    List<Coupon> findTodayCouponsByUniversity(
            @Param("universityId") Long universityId,
            @Param("since") LocalDateTime since,
            @Param("now") LocalDateTime now
    );

    // 여러 상점의 현재 유효한(활성 상태, 기간 내) 쿠폰 목록 조회
    @Query("SELECT c FROM Coupon c " +
           "WHERE c.store.id IN :storeIds " +
           "AND c.issueStartsAt <= :now AND c.issueEndsAt >= :now " +
           "AND c.status = 'ACTIVE'")
    List<Coupon> findActiveCouponsByStoreIds(
            @Param("storeIds") List<Long> storeIds,
            @Param("now") LocalDateTime now
    );

    // 여러 상점의 현재 유효한(활성 상태, 기간 내) 쿠폰 중 학생이 아직 다운로드하지 않은 쿠폰이 있는 상점 ID 목록 조회
    @Query("SELECT DISTINCT c.store.id FROM Coupon c " +
           "WHERE c.store.id IN :storeIds " +
           "AND c.issueStartsAt <= :now AND c.issueEndsAt >= :now " +
           "AND c.status = 'ACTIVE' " +
           "AND NOT EXISTS (SELECT 1 FROM StudentCoupon sc WHERE sc.coupon = c AND sc.user.id = :userId)")
    List<Long> findStoreIdsWithDownloadableCoupons(
            @Param("storeIds") List<Long> storeIds,
            @Param("now") LocalDateTime now,
            @Param("userId") Long userId
    );

    // 특정 상점에 현재 유효한 쿠폰이 존재하는지 여부 확인 (빠른 조회용)
    @Query("SELECT COUNT(c) > 0 FROM Coupon c " +
           "WHERE c.store.id = :storeId " +
           "AND c.issueStartsAt <= :now AND c.issueEndsAt >= :now " +
           "AND c.status = 'ACTIVE'")
    boolean existsActiveCoupon(
            @Param("storeId") Long storeId,
            @Param("now") LocalDateTime now
    );

    // 특정 상점에 학생이 아직 다운로드하지 않은 현재 유효한 쿠폰이 존재하는지 여부 확인 (빠른 조회용)
    @Query("SELECT COUNT(c) > 0 FROM Coupon c " +
           "WHERE c.store.id = :storeId " +
           "AND c.issueStartsAt <= :now AND c.issueEndsAt >= :now " +
           "AND c.status = 'ACTIVE' " +
           "AND NOT EXISTS (SELECT 1 FROM StudentCoupon sc WHERE sc.coupon = c AND sc.user.id = :userId)")
    boolean existsDownloadableCoupon(
            @Param("storeId") Long storeId,
            @Param("now") LocalDateTime now,
            @Param("userId") Long userId
    );

    // ACTIVE 쿠폰 중 발급 가능 기간 지난 쿠폰 EXPIRED로 설정
    @Modifying
    @Query("UPDATE Coupon c SET c.status = 'EXPIRED' WHERE c.status = 'ACTIVE' AND c.issueEndsAt < :now")
    void expireByIssueEndsAt(@Param("now") LocalDateTime now);

}
