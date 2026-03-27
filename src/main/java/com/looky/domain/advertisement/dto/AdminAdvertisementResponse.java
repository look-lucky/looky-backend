package com.looky.domain.advertisement.dto;

import com.looky.domain.advertisement.entity.Advertisement;
import com.looky.domain.advertisement.entity.AdvertisementStatus;
import com.looky.domain.advertisement.entity.AdvertisementType;
import com.looky.domain.user.entity.Gender;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminAdvertisementResponse {

    private Long id;
    private String title;
    private AdvertisementType advertisementType;
    private String imageUrl;
    private String landingUrl;
    private AdvertisementStatus status;
    private Integer displayOrder;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private Long targetUniversityId;
    private String targetUniversityName;
    private Long targetOrganizationId;
    private String targetOrganizationName;
    private Gender targetGender;

    private AdminAdvertisementResponse(Advertisement advertisement) {
        this.id = advertisement.getId();
        this.title = advertisement.getTitle();
        this.advertisementType = advertisement.getAdvertisementType();
        this.imageUrl = advertisement.getImageUrl();
        this.landingUrl = advertisement.getLandingUrl();
        this.status = advertisement.getStatus();
        this.displayOrder = advertisement.getDisplayOrder();
        this.startAt = advertisement.getStartAt();
        this.endAt = advertisement.getEndAt();
        this.createdAt = advertisement.getCreatedAt();
        if (advertisement.getTargetUniversity() != null) {
            this.targetUniversityId = advertisement.getTargetUniversity().getId();
            this.targetUniversityName = advertisement.getTargetUniversity().getName();
        }
        if (advertisement.getTargetOrganization() != null) {
            this.targetOrganizationId = advertisement.getTargetOrganization().getId();
            this.targetOrganizationName = advertisement.getTargetOrganization().getName();
        }
        this.targetGender = advertisement.getTargetGender();
    }

    public static AdminAdvertisementResponse from(Advertisement advertisement) {
        return new AdminAdvertisementResponse(advertisement);
    }
}
