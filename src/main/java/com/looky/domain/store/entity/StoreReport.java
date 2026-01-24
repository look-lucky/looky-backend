package com.looky.domain.store.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ElementCollection(targetClass = StoreReportReason.class)
    @CollectionTable(name = "store_report_reason", joinColumns = @JoinColumn(name = "store_report_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private Set<StoreReportReason> reasons = new HashSet<>();

    @Column(length = 300)
    private String detail; // 상세 사유 (기타 선택 시 필수, 그 외 선택)

    @Builder
    public StoreReport(Store store, User reporter, Set<StoreReportReason> reasons, String detail) {
        this.store = store;
        this.reporter = reporter;
        this.reasons = reasons;
        this.detail = detail;
    }
}
