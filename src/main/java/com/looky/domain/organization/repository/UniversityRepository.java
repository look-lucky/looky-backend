package com.looky.domain.organization.repository;

import com.looky.domain.organization.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UniversityRepository extends JpaRepository<University, Long> {
    List<University> findAllByOrderByNameAsc();
}
