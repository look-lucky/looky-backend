package com.looky.domain.coupon.repository;

import com.looky.domain.coupon.entity.CouponItem;
import com.looky.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouponItemRepository extends JpaRepository<CouponItem, Long> {
    List<CouponItem> findByItem(Item item);
}
