package com.looky.domain.review.repository;

import com.looky.domain.review.entity.Review;
import com.looky.domain.review.entity.ReviewLike;
import com.looky.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByUserAndReview(User user, Review review);

    void deleteByUserAndReview(User user, Review review);
}
