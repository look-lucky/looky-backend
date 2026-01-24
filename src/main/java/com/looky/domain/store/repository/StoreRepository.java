package com.looky.domain.store.repository;

import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Page<Store> findAll(Pageable pageable);

    boolean existsByName(String name);

    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    Page<Store> findByNameContaining(String keyword, Pageable pageable);

    Page<Store> findByStoreCategoriesContains(StoreCategory category, Pageable pageable);

    Page<Store> findByNameContainingAndStoreCategoriesContains(String keyword, StoreCategory category, Pageable pageable);

    List<Store> findAllByUser(User user);
}
