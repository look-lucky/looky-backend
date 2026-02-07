package com.looky.domain.organization.repository;

import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.UserOrganization;
import com.looky.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import com.looky.domain.organization.entity.OrganizationCategory;

public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {
    boolean existsByUserAndOrganization(User user, Organization organization);

    Optional<UserOrganization> findByUserAndOrganization(User user, Organization organization);

    // 유저의 모든 소속 단체 조회
    List<UserOrganization> findAllByUser(User user);
    
    // 특정 카테고리의 조직에 속한 내역 조회 (JPQL)
    @Query("SELECT uo FROM UserOrganization uo JOIN uo.organization o WHERE uo.user = :user AND o.category = :category")
    List<UserOrganization> findByUserAndOrganizationCategory(@Param("user") User user, @Param("category") OrganizationCategory category);

    // 유저의 특정 카테고리 소속 정보 삭제
    @Modifying
    @Query("DELETE FROM UserOrganization uo WHERE uo.user = :user AND uo.organization.category = :category")
    void deleteByUserAndOrganizationCategory(@Param("user") User user, @Param("category") OrganizationCategory category);
}
