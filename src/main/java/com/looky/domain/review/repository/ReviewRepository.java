package com.looky.domain.review.repository;

import com.looky.domain.review.entity.Review;
import com.looky.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.looky.domain.user.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = { "user" })
    Page<Review> findByStoreAndParentReviewIsNull(Store store, Pageable pageable);

    @EntityGraph(attributePaths = { "store" })
    Page<Review> findByUserAndParentReviewIsNull(User user, Pageable pageable);

    boolean existsByUserAndStoreAndParentReviewIsNull(User user, Store store);

    Long countByStoreIdAndParentReviewIsNull(Long storeId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.id = :storeId AND r.parentReview IS NULL")
    Double findAverageRatingByStoreId(@Param("storeId") Long storeId);

    Long countByStoreIdAndRatingAndParentReviewIsNull(Long storeId, Integer rating);
}
