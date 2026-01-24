package com.looky.domain.store.entity;

import com.looky.domain.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "store_organization", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "store_id", "organization_id" })
})
public class StoreOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    private String benefit;

    @Builder
    public StoreOrganization(Store store, Organization organization, String benefit) {
        this.store = store;
        this.organization = organization;
        this.benefit = benefit;
    }

    public void updateBenefit(String benefit) {
        this.benefit = benefit;
    }
}
