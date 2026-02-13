package com.looky.domain.store.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

import com.looky.domain.organization.entity.University;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;

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

    @Formula("(SELECT COALESCE(AVG(r.rating), 0) FROM review r WHERE r.store_id = store_id AND r.parent_review_id IS NULL)")
    private Double averageRating; // 평균 별점 (Formula)

    private String representativeName; // 대표자명

    private Boolean needToCheck; // 관리자 확인 필요 (엑셀 자동 등록 시)

    private String checkReason; // 확인 필요 사유 (엑셀 자동 등록 시)

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

    @ElementCollection
    @CollectionTable(name = "store_holidays", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "holiday_date")
    private List<LocalDate> holidayDates = new ArrayList<>(); // 휴무일 리스트

    @Column(nullable = false)
    private Boolean isSuspended = false; // 영업 중지 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CloverGrade cloverGrade = CloverGrade.SEED; // 클로버 등급 (Default: SEED)

    @Builder
    public Store(User user, String name, String branch, String roadAddress, String jibunAddress, String bizRegNo, Double latitude, Double longitude, String storePhone, String introduction, String operatingHours, Set<StoreCategory> storeCategories, Set<StoreMood> storeMoods, StoreStatus storeStatus, Boolean needToCheck, String checkReason, List<LocalDate> holidayDates, Boolean isSuspended, String representativeName) {
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
        this.needToCheck = needToCheck;
        this.checkReason = checkReason;
        this.holidayDates = holidayDates != null ? holidayDates : new ArrayList<>();
        this.isSuspended = isSuspended != null ? isSuspended : false;
        this.representativeName = representativeName;
    }

    public void updateStore(String name, String branch, String roadAddress, String jibunAddress, Double latitude, Double longitude, String phone, String introduction, String operatingHours, Set<StoreCategory> storeCategories, Set<StoreMood> storeMoods, List<LocalDate> holidayDates, Boolean isSuspended, String representativeName) {
        this.name = name;
        this.branch = branch;
        this.roadAddress = roadAddress;
        this.jibunAddress = jibunAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.storePhone = phone;
        this.introduction = introduction;
        this.operatingHours = operatingHours;
        this.isSuspended = isSuspended;
        this.representativeName = representativeName;

        if (storeCategories != null) {
            this.storeCategories.clear(); // JPA 영속성 컨텍스트 유지를 위해 컬렉션 전체 교체 대신 내용물 교체
            this.storeCategories.addAll(storeCategories);
        }
        if (storeMoods != null) {
            this.storeMoods.clear(); // JPA 영속성 컨텍스트 유지를 위해 컬렉션 전체 교체 대신 내용물 교체
            this.storeMoods.addAll(storeMoods);
        }
        
        if (holidayDates != null) {
            this.holidayDates.clear();
            this.holidayDates.addAll(holidayDates);
        }
        
    }

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreUniversity> universities = new ArrayList<>();

    // 연관관계 편의 메서드
    
    public void addUniversity(University university) {
        // 중복 체크
        boolean exists = this.universities.stream()
                .anyMatch(su -> su.getUniversity().getId().equals(university.getId()));
        
        if (!exists) {
            StoreUniversity storeUniversity = StoreUniversity.builder()
                    .store(this)
                    .university(university)
                    .build();
            this.universities.add(storeUniversity);
        }
    }

    public void addImage(StoreImage image) {
        this.images.add(image);
    }

    public void removeImage(StoreImage image) {
        this.images.remove(image);
    }

    public void approveClaim(User owner, String bizRegNo, String storePhone, String representativeName) {
        // 승인된 가게 점유 요청 정보로 업데이트
        this.user = owner;
        this.bizRegNo = bizRegNo;
        this.storePhone = storePhone;
        this.storeStatus = StoreStatus.ACTIVE;
        this.needToCheck = false;
        this.checkReason = null;
        this.representativeName = representativeName;
    }

    public void unclaim() {
        this.user = null;
        this.bizRegNo = null;
        this.storePhone = null;
        this.representativeName = null;
        this.storeStatus = StoreStatus.UNCLAIMED;
        this.needToCheck = false;
        this.checkReason = null;
        this.introduction = null;
        this.operatingHours = null;
        this.storeCategories.clear();
        this.storeMoods.clear();
        this.images.clear();
        this.holidayDates.clear();
        this.isSuspended = false;
        this.cloverGrade = CloverGrade.SEED;
    }

    public void markAsNeedCheck(String reason) {
        this.needToCheck = true;
        this.checkReason = reason;
    }

    public void updateCloverGrade(CloverGrade cloverGrade) {
        this.cloverGrade = cloverGrade;
    }

    public void updateLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
