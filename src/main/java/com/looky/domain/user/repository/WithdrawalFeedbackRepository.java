package com.looky.domain.user.repository;

import com.looky.domain.user.entity.WithdrawalFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalFeedbackRepository extends JpaRepository<WithdrawalFeedback, Long> {
}
