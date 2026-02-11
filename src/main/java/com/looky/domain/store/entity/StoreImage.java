package com.looky.domain.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int orderIndex; // 0일 경우 썸네일

    @Builder
    public StoreImage(Store store, String imageUrl, int orderIndex) {
        this.store = store;
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
    }

    public void updateOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
