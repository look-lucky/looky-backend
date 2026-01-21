package com.neardeal.domain.user.repository;

import com.neardeal.domain.user.entity.CouncilProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouncilProfileRepository extends JpaRepository<CouncilProfile, Long> {
}
