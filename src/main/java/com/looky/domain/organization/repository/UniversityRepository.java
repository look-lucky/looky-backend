package com.looky.domain.organization.repository;

import com.looky.domain.organization.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Long> {
}
