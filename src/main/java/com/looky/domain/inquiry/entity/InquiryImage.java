package com.looky.domain.inquiry.entity;

import com.looky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inquiry_image")
public class InquiryImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int orderIndex;

    @Builder
    public InquiryImage(Inquiry inquiry, String imageUrl, int orderIndex) {
        this.inquiry = inquiry;
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
    }
}
