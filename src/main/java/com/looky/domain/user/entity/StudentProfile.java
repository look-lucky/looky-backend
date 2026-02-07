package com.looky.domain.user.entity;

import com.looky.domain.organization.entity.University;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentProfile {

    @Id
    private Long userId;

    @Column
    private String nickname;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // User의 PK를 이 테이블의 PK이자 FK로 사용
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;


    @Column(nullable = false)
    private boolean isClubMember;

    @Builder
    public StudentProfile(User user, String nickname, University university, boolean isClubMember) {
        this.user = user;
        this.nickname = nickname;
        this.university = university;
        this.isClubMember = isClubMember;
    }

    public void update(String nickname, boolean isClubMember, University university) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        this.isClubMember = isClubMember;
        if (university != null) {
            this.university = university;
        }
    }
}
