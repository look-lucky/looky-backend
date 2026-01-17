package com.neardeal.domain.storenews.dto;

import com.neardeal.domain.storenews.entity.StoreNews;
import com.neardeal.domain.storenews.entity.StoreNewsImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StoreNewsResponse {

    private Long id;
    private Long storeId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
    private int likeCount;
    private int commentCount;
    private boolean isLiked;

    public StoreNewsResponse(StoreNews news, boolean isLiked) {
        this.id = news.getId();
        this.storeId = news.getStore().getId();
        this.title = news.getTitle();
        this.content = news.getContent();
        this.createdAt = news.getCreatedAt();
        this.imageUrls = news.getImages().stream()
                .map(StoreNewsImage::getImageUrl)
                .collect(Collectors.toList());
        this.likeCount = news.getLikeCount();
        this.commentCount = news.getCommentCount();
        this.isLiked = isLiked;
    }

    public static StoreNewsResponse from(StoreNews news, boolean isLiked) {
        return new StoreNewsResponse(news, isLiked);
    }
}
