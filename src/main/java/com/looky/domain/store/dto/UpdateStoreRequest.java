package com.looky.domain.store.dto;

import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreMood;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStoreRequest {

    private String name;
    private String branch;

    private String roadAddress;
    private String jibunAddress;

    private Double latitude;
    private Double longitude;

    private String phone;

    private String introduction;

    private String operatingHours;

    private List<StoreCategory> storeCategories;

    private List<StoreMood> storeMoods;
}
