package com.looky.domain.storenews.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.store.entity.Store;
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
@Table(name = "store_news")
public class StoreNews extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "storeNews", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreNewsImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "storeNews", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreNewsComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "storeNews", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreNewsLike> likes = new ArrayList<>();

    private int likeCount = 0;
    private int commentCount = 0;

    @Builder
    public StoreNews(Store store, String title, String content) {
        this.store = store;
        this.title = title;
        this.content = content;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void addImage(StoreNewsImage image) {
        this.images.add(image);
        image.setStoreNews(this);
    }

    public void clearImages() {
        this.images.clear();
    }

    public void removeImage(StoreNewsImage image) {
        this.images.remove(image);
        image.setStoreNews(null);
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
