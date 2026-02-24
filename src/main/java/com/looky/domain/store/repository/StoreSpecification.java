package com.looky.domain.store.repository;

import com.looky.domain.partnership.entity.Partnership;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreMood;
import com.looky.domain.store.entity.StoreStatus;
import jakarta.persistence.criteria.SetJoin;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class StoreSpecification {

    public static Specification<Store> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return cb.like(root.get("name"), "%" + keyword + "%");
        };
    }

    public static Specification<Store> hasCategories(List<StoreCategory> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }
            SetJoin<Store, StoreCategory> categoryJoin = root.joinSet("storeCategories");
            query.distinct(true);
            return categoryJoin.in(categories);
        };
    }

    public static Specification<Store> hasMoods(List<StoreMood> moods) {
        return (root, query, cb) -> {
            if (moods == null || moods.isEmpty()) {
                return null;
            }
            SetJoin<Store, StoreMood> moodJoin = root.joinSet("storeMoods");
            query.distinct(true);
            return moodJoin.in(moods);
        };
    }

    public static Specification<Store> hasUniversityId(Long universityId) {
        return (root, query, cb) -> {
            if (universityId == null) {
                return null;
            }
            var universityJoin = root.join("universities");
            query.distinct(true);
            return cb.equal(universityJoin.get("university").get("id"), universityId);
        };
    }

    public static Specification<Store> isNotSuspended() {
        return (root, query, cb) -> cb.isFalse(root.get("isSuspended"));
    }

    public static Specification<Store> hasStoreStatus(StoreStatus storeStatus) {
        return (root, query, cb) -> {
            if (storeStatus == null) return null;
            return cb.equal(root.get("storeStatus"), storeStatus);
        };
    }

    public static Specification<Store> hasPartnership(Boolean hasPartnership) {
        return (root, query, cb) -> {
            if (hasPartnership == null) return null;
            var subquery = query.subquery(Long.class);
            var partnership = subquery.from(Partnership.class);
            subquery.select(partnership.get("store").get("id"))
                    .where(cb.equal(partnership.get("store").get("id"), root.get("id")));
            return hasPartnership ? cb.exists(subquery) : cb.not(cb.exists(subquery));
        };
    }
}
