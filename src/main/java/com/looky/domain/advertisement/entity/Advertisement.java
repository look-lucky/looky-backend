package com.looky.domain.advertisement.entity;

import com.looky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "advertisement")
public class Advertisement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advertisement_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "advertisement_type", nullable = false)
    private AdvertisementType advertisementType;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "landing_url")
    private String landingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdvertisementStatus status;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Builder
    public Advertisement(String title, AdvertisementType advertisementType, String imageUrl, String landingUrl,
                         AdvertisementStatus status, Integer displayOrder, LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.advertisementType = advertisementType;
        this.imageUrl = imageUrl;
        this.landingUrl = landingUrl;
        this.status = status;
        this.displayOrder = displayOrder;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void update(String title, String imageUrl, String landingUrl,
                       Integer displayOrder, LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.landingUrl = landingUrl;
        this.displayOrder = displayOrder;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void updateStatus(AdvertisementStatus status) {
        this.status = status;
    }
}
