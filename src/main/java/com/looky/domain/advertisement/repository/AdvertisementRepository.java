package com.looky.domain.advertisement.repository;

import com.looky.domain.advertisement.entity.Advertisement;
import com.looky.domain.advertisement.entity.AdvertisementStatus;
import com.looky.domain.advertisement.entity.AdvertisementType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long>, JpaSpecificationExecutor<Advertisement> {

    List<Advertisement> findAllByAdvertisementTypeAndStatusOrderByDisplayOrderAscIdAsc(AdvertisementType type, AdvertisementStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Advertisement> findAllByAdvertisementTypeOrderByDisplayOrderAscIdAsc(AdvertisementType type);

    List<Advertisement> findAllByStatusAndStartAtLessThanEqual(AdvertisementStatus status, LocalDateTime now);

    List<Advertisement> findAllByStatusAndEndAtLessThan(AdvertisementStatus status, LocalDateTime now);
}
