package com.looky.domain.advertisement.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.University;
import com.looky.domain.user.entity.Gender;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_university_id")
    private University targetUniversity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_organization_id")
    private Organization targetOrganization;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_gender")
    private Gender targetGender;

    @Builder
    public Advertisement(String title, AdvertisementType advertisementType, String imageUrl, String landingUrl,
                         AdvertisementStatus status, Integer displayOrder, LocalDateTime startAt, LocalDateTime endAt,
                         University targetUniversity, Organization targetOrganization, Gender targetGender) {
        this.title = title;
        this.advertisementType = advertisementType;
        this.imageUrl = imageUrl;
        this.landingUrl = landingUrl;
        this.status = status;
        this.displayOrder = displayOrder;
        this.startAt = startAt;
        this.endAt = endAt;
        this.targetUniversity = targetUniversity;
        this.targetOrganization = targetOrganization;
        this.targetGender = targetGender;
    }

    public void update(String title, String imageUrl, String landingUrl,
                       Integer displayOrder, LocalDateTime startAt, LocalDateTime endAt,
                       University targetUniversity, Organization targetOrganization, Gender targetGender) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.landingUrl = landingUrl;
        this.displayOrder = displayOrder;
        this.startAt = startAt;
        this.endAt = endAt;
        this.targetUniversity = targetUniversity;
        this.targetOrganization = targetOrganization;
        this.targetGender = targetGender;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void updateStatus(AdvertisementStatus status) {
        this.status = status;
    }
}
