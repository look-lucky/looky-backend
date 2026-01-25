package com.looky.domain.store.dto;

import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreMood;
import com.looky.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStoreRequest {

    @NotBlank(message = "가게 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "사업자등록번호는 필수입니다.")
    private String bizRegNo;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    private Double latitude;

    private Double longitude;

    private String storePhone;

    private String introduction;

    private String operatingHours;

    @NotNull(message = "카테고리는 필수입니다.")
    private List<StoreCategory> storeCategories;

    private List<StoreMood> storeMoods;

    public Store toEntity(User user) {
        return Store.builder()
                .user(user)
                .name(name)
                .bizRegNo(bizRegNo)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .storePhone(storePhone)
                .introduction(introduction)
                .operatingHours(operatingHours)
                .storeCategories(new HashSet<>(storeCategories))
                .storeMoods(storeMoods != null ? new HashSet<>(storeMoods) : new HashSet<>())
                .build();
    }
}
