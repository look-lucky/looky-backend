package com.looky.domain.store.repository;

import com.looky.domain.store.entity.Store;
import com.looky.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, JpaSpecificationExecutor<Store> {
    Page<Store> findAll(Pageable pageable);

    boolean existsByName(String name);

    boolean existsByBizRegNo(String bizRegNo);

    List<Store> findAllByUser(User user);

    Optional<Store> findFirstByNameAndRoadAddress(String name, String roadAddress);

    boolean existsByNameAndRoadAddress(String name, String roadAddress);

    List<Store> findByLatitudeAndLongitude(Double latitude, Double longitude);

    @Query(value =
        "SELECT *, " +
        "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(latitude)))) AS distance " +
        "FROM store " +
        "WHERE is_suspended = false " +
        "HAVING distance <= :radius " +
        "ORDER BY distance",
        nativeQuery = true)
    List<Store> findByLocationWithin(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("radius") double radius);

    @Query("SELECT s FROM Store s " +
           "WHERE s.storeStatus = 'UNCLAIMED' " +
           "AND (s.name LIKE %:keyword% OR s.roadAddress LIKE %:keyword% OR s.jibunAddress LIKE %:keyword%)")
    List<Store> findUnclaimedByNameOrAddress(@Param("keyword") String keyword);
}
