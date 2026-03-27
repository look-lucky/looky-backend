package com.looky.domain.advertisement.dto;

import com.looky.domain.advertisement.entity.Advertisement;
import com.looky.domain.advertisement.entity.AdvertisementStatus;
import com.looky.domain.advertisement.entity.AdvertisementType;
import com.looky.domain.user.entity.Gender;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<TargetUniversityInfo> targetUniversities;
    private List<TargetOrganizationInfo> targetOrganizations;
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
        this.targetUniversities = advertisement.getTargetUniversities().stream()
                .map(tu -> new TargetUniversityInfo(tu.getUniversity().getId(), tu.getUniversity().getName()))
                .toList();
        this.targetOrganizations = advertisement.getTargetOrganizations().stream()
                .map(to -> new TargetOrganizationInfo(to.getOrganization().getId(), to.getOrganization().getName()))
                .toList();
        this.targetGender = advertisement.getTargetGender();
    }

    public static AdminAdvertisementResponse from(Advertisement advertisement) {
        return new AdminAdvertisementResponse(advertisement);
    }

    public record TargetUniversityInfo(Long id, String name) {}

    public record TargetOrganizationInfo(Long id, String name) {}
}
