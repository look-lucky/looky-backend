package com.looky.domain.favorite.repository;

import com.looky.domain.favorite.entity.FavoriteStore;
import com.looky.domain.store.entity.Store;
import com.looky.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<FavoriteStore, Long> {

    boolean existsByUserAndStore(User user, Store store);

    void deleteByUserAndStore(User user, Store store);

    long countByStore(Store store);

    long countByStoreAndCreatedAtAfter(Store store, LocalDateTime createdAt);

    Page<FavoriteStore> findByUser(User user, Pageable pageable);

    @Query("SELECT f.store, COUNT(f) " +
           "FROM FavoriteStore f " +
           "JOIN f.store s " +
           "JOIN s.universities su " +
           "WHERE su.university.id = :universityId " +
           "AND f.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY f.store " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> findHotStores(
            @Param("universityId") Long universityId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
