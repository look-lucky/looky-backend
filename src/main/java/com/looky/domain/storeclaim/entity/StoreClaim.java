package com.looky.domain.storeclaim.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.store.entity.Store;
import com.looky.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "biz_reg_no", nullable = false)
    private String bizRegNo;

    @Column(nullable = false)
    private String representativeName;

    @Column(nullable = false)
    private String storeName;

    private String storePhone;

    @Column(nullable = false)
    private String licenseImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreClaimStatus status;

    private String rejectReason;

    @Lob
    private String adminMemo;

    @Builder
    public StoreClaim(Store store, User user, String bizRegNo, String representativeName, String storeName, String storePhone, String licenseImageUrl, StoreClaimStatus status) {
        this.store = store;
        this.user = user;
        this.bizRegNo = bizRegNo;
        this.representativeName = representativeName;
        this.storeName = storeName;
        this.storePhone = storePhone;
        this.licenseImageUrl = licenseImageUrl;
        this.status = status != null ? status : StoreClaimStatus.PENDING;
    }

    public void updateStatus(StoreClaimStatus status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public void updateAdminMemo(String adminMemo) {
        this.adminMemo = adminMemo;
    }
}
