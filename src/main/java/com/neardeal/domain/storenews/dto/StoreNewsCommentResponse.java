package com.neardeal.domain.storenews.dto;

import com.neardeal.domain.storenews.entity.StoreNewsComment;
import com.neardeal.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoreNewsCommentResponse {

    private Long id;
    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private boolean isMine;

    public StoreNewsCommentResponse(StoreNewsComment comment, User currentUser) {
        this.id = comment.getId();
        this.userId = comment.getUser().getId();
        this.nickname = comment.getUser().getName();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.isMine = currentUser != null && comment.getUser().getId().equals(currentUser.getId());
    }

    public static StoreNewsCommentResponse from(StoreNewsComment comment, User currentUser) {
        return new StoreNewsCommentResponse(comment, currentUser);
    }
}
