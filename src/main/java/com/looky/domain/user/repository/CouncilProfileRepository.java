package com.looky.domain.user.repository;

import com.looky.domain.user.entity.CouncilProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouncilProfileRepository extends JpaRepository<CouncilProfile, Long> {
}
