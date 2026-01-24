package com.looky.domain.review.repository;

import com.looky.domain.review.entity.Review;
import com.looky.domain.review.entity.ReviewReport;
import com.looky.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByReviewAndReporter(Review review, User reporter);
}
