package com.looky.domain.store.entity;

import com.looky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreClaimRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_claim_request_id")
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String businessNumber;

    @Column(nullable = false)
    private String representativeName; // 대표자명

    @Column(nullable = false)
    private String licenseImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimRequestStatus status;

    private String rejectReason; // 반려 사유

    @Lob
    private String adminMemo; // 관리자 전용 메모
}
