package com.looky.domain.store.entity;

import com.looky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_claim_request_id")
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "biz_reg_no", nullable = false)
    private String bizRegNo;

    @Column(nullable = false)
    private String representativeName; // 대표자명

    @Column(nullable = false)
    private String storeName;   // 상호명

    private String storePhone;  // 가게 전화번호

    @Column(nullable = false)
    private String licenseImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreClaimStatus status;

    private String rejectReason; // 반려 사유

    @Lob
    private String adminMemo; // 관리자 전용 메모

    @Builder
    public StoreClaim(Long storeId, Long userId, String bizRegNo, String representativeName, String storeName, String storePhone, String licenseImageUrl, StoreClaimStatus status) {
        this.storeId = storeId;
        this.userId = userId;
        this.bizRegNo = bizRegNo;
        this.representativeName = representativeName;
        this.storeName = storeName;
        this.storePhone = storePhone;
        this.licenseImageUrl = licenseImageUrl;
        this.status = status != null ? status : StoreClaimStatus.PENDING;
    }
}
