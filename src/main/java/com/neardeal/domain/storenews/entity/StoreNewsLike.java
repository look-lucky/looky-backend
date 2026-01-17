package com.neardeal.domain.storenews.entity;

import com.neardeal.common.entity.BaseEntity;
import com.neardeal.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreNewsLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_news_id", nullable = false)
    private StoreNews storeNews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public StoreNewsLike(StoreNews storeNews, User user) {
        this.storeNews = storeNews;
        this.user = user;
    }
}
