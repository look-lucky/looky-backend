package com.looky.domain.review.repository;

import com.looky.domain.review.entity.Review;
import com.looky.domain.review.entity.ReviewLike;
import com.looky.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByUserAndReview(User user, Review review);

    List<ReviewLike> findByUserAndReviewIn(User user, java.util.Collection<Review> reviews);

    void deleteByUserAndReview(User user, Review review);
}
