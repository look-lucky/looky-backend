package com.looky.domain.partnership.repository;

import com.looky.domain.partnership.entity.Partnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PartnershipRepository extends JpaRepository<Partnership, Long> {

    boolean existsByStoreIdAndOrganizationId(Long storeId, Long organizationId);

    // 복합키 조회용
    Partnership findByStoreIdAndOrganizationId(Long storeId, Long organizationId);

    // 대학별 제휴 조회
    @Query("SELECT p FROM Partnership p " +
            "JOIN FETCH p.store s " +
            "JOIN FETCH p.organization o " +
            "JOIN FETCH o.university u " +
            "WHERE u.id = :universityId " +
            "ORDER BY p.id DESC")
    List<Partnership> findAllByOrganizationUniversityId(
            @Param("universityId") Long universityId);

    // 대학 및 조직별 제휴 조회
    @Query("SELECT p FROM Partnership p " +
            "JOIN FETCH p.store s " +
            "JOIN FETCH p.organization o " +
            "JOIN FETCH o.university u " +
            "WHERE o.id = :organizationId AND u.id = :universityId " +
            "ORDER BY p.id DESC")
    List<Partnership> findAllByOrganizationIdAndOrganizationUniversityId(
            @Param("organizationId") Long organizationId,
            @Param("universityId") Long universityId
    );

    @Query("SELECT p FROM Partnership p " +
           "JOIN FETCH p.organization o " +
           "WHERE p.store.id IN :storeIds " +
           "AND o.university.id = :universityId " +
           "AND p.startsAt <= :today AND p.endsAt >= :today")
    List<Partnership> findActivePartnershipsByStoreIdsAndUniversityId(
            @Param("storeIds") List<Long> storeIds,
            @Param("universityId") Long universityId,
            @Param("today") LocalDate today
    );

    @Query("SELECT COUNT(p) > 0 FROM Partnership p " +
           "JOIN p.organization o " +
           "WHERE p.store.id = :storeId " +
           "AND o.university.id = :universityId " +
           "AND p.startsAt <= :today AND p.endsAt >= :today")
    boolean existsActivePartnership(
            @Param("storeId") Long storeId,
            @Param("universityId") Long universityId,
            @Param("today") LocalDate today
    );

    // 상점 상세 페이지 조회용
    @Query("SELECT p FROM Partnership p " +
           "JOIN FETCH p.organization o " +
           "WHERE p.store.id = :storeId " +
           "AND p.startsAt <= :today AND p.endsAt >= :today")
    List<Partnership> findAllActiveByStoreId(
            @Param("storeId") Long storeId,
            @Param("today") LocalDate today
    );

    // 내 소속 조직과 매칭되는 활성 제휴 목록 조회용
    @Query("SELECT p FROM Partnership p " +
           "JOIN FETCH p.organization o " +
           "WHERE p.store.id IN :storeIds " +
           "AND o.id IN :organizationIds " +
           "AND p.startsAt <= :today AND p.endsAt >= :today")
    List<Partnership> findActivePartnershipsByStoreIdsAndOrganizationIds(
            @Param("storeIds") List<Long> storeIds,
            @Param("organizationIds") List<Long> organizationIds,
            @Param("today") LocalDate today
    );
}
