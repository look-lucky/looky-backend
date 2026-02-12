package com.looky.domain.item.repository;

import com.looky.domain.item.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    List<ItemCategory> findByStoreId(Long storeId);

    void deleteByStoreId(Long storeId);
}
