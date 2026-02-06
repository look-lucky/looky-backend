package com.looky.domain.inquiry.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inquiry")
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryType type;

    @Column(nullable = false, length = 14)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryImage> images = new ArrayList<>();

    @Builder
    public Inquiry(User user, InquiryType type, String title, String content) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
    }

    public void addImage(InquiryImage image) {
        this.images.add(image);
    }
}
