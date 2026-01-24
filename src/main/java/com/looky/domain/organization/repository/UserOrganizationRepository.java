package com.looky.domain.organization.repository;

import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.UserOrganization;
import com.looky.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {
    boolean existsByUserAndOrganization(User user, Organization organization);

    Optional<UserOrganization> findByUserAndOrganization(User user, Organization organization);
}
