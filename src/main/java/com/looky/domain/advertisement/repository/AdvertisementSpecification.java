package com.looky.domain.advertisement.repository;

import com.looky.domain.advertisement.entity.Advertisement;
import com.looky.domain.advertisement.entity.AdvertisementStatus;
import com.looky.domain.advertisement.entity.AdvertisementType;
import org.springframework.data.jpa.domain.Specification;

public class AdvertisementSpecification {

    public static Specification<Advertisement> hasType(AdvertisementType type) {
        return (root, query, cb) -> {
            if (type == null) {
                return null;
            }
            return cb.equal(root.get("advertisementType"), type);
        };
    }

    public static Specification<Advertisement> hasStatus(AdvertisementStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }
}
