package com.looky.domain.coupon.repository;

import com.looky.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    // 특정 상점이 발행한 모든 쿠폰 목록 조회
    List<Coupon> findByStoreId(Long storeId);

    // 특정 상점이 발행한 쿠폰의 총 개수 조회
    long countByStoreId(Long storeId);

    // 사용자의 학교와 제휴된 상점에서 오늘 발급 시작된 쿠폰 목록 조회 (제휴 기간 내, 오늘 날짜 기준)
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

    // 여러 상점의 현재 유효한(활성 상태, 기간 내) 쿠폰 목록 조회
    @Query("SELECT c FROM Coupon c " +
           "WHERE c.store.id IN :storeIds " +
           "AND c.issueStartsAt <= :now AND c.issueEndsAt >= :now " +
           "AND c.status = 'ACTIVE'")
    List<Coupon> findActiveCouponsByStoreIds(
            @Param("storeIds") List<Long> storeIds,
            @Param("now") LocalDateTime now
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

    // 쿠폰 다운로드 시 다운로드 수 증가 (선착순 마감 처리, 동시성 제어)
    // @Modifying: UPDATE/DELETE 쿼리임을 명시
    // clearAutomatically = true: 쿼리 실행 후 영속성 컨텍스트(1차 캐시)를 비워서 DB와 동기화 (조회 시 최신 데이터 보장)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Coupon c SET c.downloadCount = c.downloadCount + 1 " +
           "WHERE c.id = :id AND c.downloadCount < c.totalQuantity")
    int incrementDownloadCount(@Param("id") Long id);
}
