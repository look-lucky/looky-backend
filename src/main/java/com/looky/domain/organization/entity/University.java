package com.looky.domain.organization.entity;

import jakarta.persistence.*;
import lombok.*;

/*
마스터 테이블 (Admin 관리)
*/
@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "university_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "email_domain", nullable = false)
    private String emailDomains; // 콤마로 구분된 도메인 목록

    @Builder
    public University(String name, String emailDomains) {
        this.name = name;
        this.emailDomains = emailDomains;
    }

    public void update(String name, String emailDomains) {
        this.name = name;
        this.emailDomains = emailDomains;
    }
}
