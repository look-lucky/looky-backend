package com.looky.domain.organization.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.looky.common.entity.BaseEntity;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationCategory category; // 단과대학, 학과, 동아리 등

    @Column(nullable = false)
    private String name; // 공과대학, 소프트웨어공학과 등

    private LocalDateTime expiresAt; // 제휴 만료 시점

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Organization parent;

    @OneToMany(mappedBy = "parent")
    private List<Organization> children = new ArrayList<>();

    @Builder
    public Organization(University university, OrganizationCategory category, String name, LocalDateTime expiresAt, Organization parent) {
        this.university = university;
        this.category = category;
        this.name = name;
        this.expiresAt = expiresAt;
        this.parent = parent;
    }

    public void update(OrganizationCategory category, String name, LocalDateTime expiresAt, Organization parent) {
        this.category = category;
        this.name = name;
        this.expiresAt = expiresAt;
        this.parent = parent;
    }
}
