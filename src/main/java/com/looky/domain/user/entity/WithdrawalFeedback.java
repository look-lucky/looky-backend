package com.looky.domain.user.entity;

import com.looky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class WithdrawalFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @ElementCollection(targetClass = WithdrawalReason.class)
    @CollectionTable(name = "withdrawal_reason", joinColumns = @JoinColumn(name = "feedback_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private List<WithdrawalReason> reasons = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String detailReason;
}
