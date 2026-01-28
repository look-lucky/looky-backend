package com.looky.domain.store.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "store")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(nullable = false)
    private String name; // 상호명

    private String branch; // 지점명

    @Column(name = "biz_reg_no")
    private String bizRegNo; // 사업자등록번호

    @Column(name = "road_address", nullable = false)
    private String roadAddress; // 도로명 주소

    @Column(name = "jibun_address")
    private String jibunAddress; // 지번 주소

    private Double latitude; // 위도

    private Double longitude; // 경도

    private String storePhone; // 가게 전화 번호

    private Boolean needToCheck; // 관리자 확인 필요 (엑셀 자동 등록 시)

    @Lob
    private String introduction;

    @Lob
    private String operatingHours; // JSON String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus storeStatus;

    @ElementCollection(targetClass = StoreCategory.class)
    @CollectionTable(name = "store_categories", joinColumns = @JoinColumn(name = "store_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Set<StoreCategory> storeCategories = new HashSet<>();

    @ElementCollection(targetClass = StoreMood.class)
    @CollectionTable(name = "store_moods", joinColumns = @JoinColumn(name = "store_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "mood")
    private Set<StoreMood> storeMoods = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") 
    private User user; // 사장님 (미등록 가게일 경우 null)

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> images = new ArrayList<>();

    @Builder
    public Store(User user, String name, String branch, String roadAddress, String jibunAddress, String bizRegNo, Double latitude, Double longitude, String storePhone, String introduction, String operatingHours, Set<StoreCategory> storeCategories, Set<StoreMood> storeMoods, StoreStatus storeStatus) {
        this.user = user;
        this.name = name;
        this.branch = branch;
        this.roadAddress = roadAddress;
        this.jibunAddress = jibunAddress;
        this.bizRegNo = bizRegNo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.storePhone = storePhone;
        this.introduction = introduction;
        this.operatingHours = operatingHours;
        this.storeCategories = storeCategories != null ? storeCategories : new HashSet<>();
        this.storeMoods = storeMoods != null ? storeMoods : new HashSet<>();
        this.storeStatus = storeStatus != null ? storeStatus : StoreStatus.UNCLAIMED;
    }

    public void updateStore(String name, String branch, String roadAddress, String jibunAddress, Double latitude, Double longitude, String phone, String introduction, String operatingHours, Set<StoreCategory> storeCategories, Set<StoreMood> storeMoods) {
        if (name != null) {
            this.name = name;
        }
        if (branch != null) {
            this.branch = branch;
        }
        if (roadAddress != null) {
            this.roadAddress = roadAddress;
        }
        if (jibunAddress != null) {
            this.jibunAddress = jibunAddress;
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
        if (phone != null) {
            this.storePhone = phone;
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
        if (operatingHours != null) {
            this.operatingHours = operatingHours;
        }
        if (storeCategories != null) {
            this.storeCategories.clear();
            this.storeCategories.addAll(storeCategories);
        }
        if (storeMoods != null) {
            this.storeMoods.clear();
            this.storeMoods.addAll(storeMoods);
        }
    }

    // 연관관계 편의 메서드
    public void addImage(StoreImage image) {
        this.images.add(image);
    }

    public void removeImage(StoreImage image) {
        this.images.remove(image);
    }

    public void approveClaim(User owner, String bizRegNo, String storePhone) {
        // 승인된 가게 점유 요청 정보로 업데이트
        this.user = owner;
        this.bizRegNo = bizRegNo;
        this.storePhone = storePhone;
        this.storeStatus = StoreStatus.ACTIVE;
    }

    public void markAsNeedCheck() {
        this.needToCheck = true;
    }
}
