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
import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "target_gender")
    private Gender targetGender;

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdvertisementUniversity> targetUniversities = new ArrayList<>();

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdvertisementOrganization> targetOrganizations = new ArrayList<>();

    @Builder
    public Advertisement(String title, AdvertisementType advertisementType, String imageUrl, String landingUrl,
                         AdvertisementStatus status, Integer displayOrder, LocalDateTime startAt, LocalDateTime endAt,
                         Gender targetGender) {
        this.title = title;
        this.advertisementType = advertisementType;
        this.imageUrl = imageUrl;
        this.landingUrl = landingUrl;
        this.status = status;
        this.displayOrder = displayOrder;
        this.startAt = startAt;
        this.endAt = endAt;
        this.targetGender = targetGender;
    }

    public void update(String title, String imageUrl, String landingUrl,
                       Integer displayOrder, LocalDateTime startAt, LocalDateTime endAt,
                       Gender targetGender) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.landingUrl = landingUrl;
        this.displayOrder = displayOrder;
        this.startAt = startAt;
        this.endAt = endAt;
        this.targetGender = targetGender;
    }

    public void addTargetUniversity(University university) {
        boolean exists = this.targetUniversities.stream()
                .anyMatch(tu -> tu.getUniversity().getId().equals(university.getId()));
        if (!exists) {
            this.targetUniversities.add(AdvertisementUniversity.builder()
                    .advertisement(this)
                    .university(university)
                    .build());
        }
    }

    public void addTargetOrganization(Organization organization) {
        boolean exists = this.targetOrganizations.stream()
                .anyMatch(to -> to.getOrganization().getId().equals(organization.getId()));
        if (!exists) {
            this.targetOrganizations.add(AdvertisementOrganization.builder()
                    .advertisement(this)
                    .organization(organization)
                    .build());
        }
    }

    public void clearTargetUniversities() {
        this.targetUniversities.clear();
    }

    public void clearTargetOrganizations() {
        this.targetOrganizations.clear();
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void updateStatus(AdvertisementStatus status) {
        this.status = status;
    }
}
