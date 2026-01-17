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
@Table(name = "store_news_comment")
public class StoreNewsComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_news_id", nullable = false)
    private StoreNews storeNews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Builder
    public StoreNewsComment(StoreNews storeNews, User user, String content) {
        this.storeNews = storeNews;
        this.user = user;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }
}
