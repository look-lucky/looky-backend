package com.looky.domain.partnership.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partnership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String benefit;

    @Column(nullable = false)
    private LocalDate startsAt; // 제휴 시작일

    @Column(nullable = false)
    private LocalDate endsAt; // 제휴 종료일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;



    @Builder
    public Partnership(String benefit, LocalDate startsAt, LocalDate endsAt, Store store, Organization organization) {
        this.benefit = benefit;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.store = store;
        this.organization = organization;
    }

    public void updateBenefit(String benefit, LocalDate startsAt, LocalDate endsAt) {
        this.benefit = benefit;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }
}
