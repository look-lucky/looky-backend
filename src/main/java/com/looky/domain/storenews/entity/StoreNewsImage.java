package com.looky.domain.storenews.entity;

import com.looky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "store_news_image")
public class StoreNewsImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_news_id", nullable = false)
    private StoreNews storeNews;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int orderIndex;

    @Builder
    public StoreNewsImage(String imageUrl, int orderIndex) {
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
    }

    public void updateOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void setStoreNews(StoreNews storeNews) {
        this.storeNews = storeNews;
    }
}
