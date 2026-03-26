package com.looky.domain.advertisement.dto;

import com.looky.domain.advertisement.entity.Advertisement;
import lombok.Getter;

@Getter
public class AdvertisementResponse {

    private Long id;
    private String imageUrl;
    private String landingUrl;
    private Integer displayOrder;

    private AdvertisementResponse(Advertisement advertisement) {
        this.id = advertisement.getId();
        this.imageUrl = advertisement.getImageUrl();
        this.landingUrl = advertisement.getLandingUrl();
        this.displayOrder = advertisement.getDisplayOrder();
    }

    public static AdvertisementResponse from(Advertisement advertisement) {
        return new AdvertisementResponse(advertisement);
    }
}
