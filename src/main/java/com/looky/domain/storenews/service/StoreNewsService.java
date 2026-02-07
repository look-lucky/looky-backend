package com.looky.domain.storenews.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.S3Service;
import com.looky.common.util.FileValidator;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.storenews.dto.CreateStoreNewsCommentRequest;
import com.looky.domain.storenews.dto.CreateStoreNewsRequest;
import com.looky.domain.storenews.dto.StoreNewsCommentResponse;
import com.looky.domain.storenews.dto.StoreNewsResponse;
import com.looky.domain.storenews.dto.UpdateStoreNewsRequest;
import com.looky.domain.storenews.entity.StoreNews;
import com.looky.domain.storenews.entity.StoreNewsComment;
import com.looky.domain.storenews.entity.StoreNewsImage;
import com.looky.domain.storenews.entity.StoreNewsLike;
import com.looky.domain.storenews.repository.StoreNewsCommentRepository;
import com.looky.domain.storenews.repository.StoreNewsLikeRepository;
import com.looky.domain.storenews.repository.StoreNewsRepository;
import com.looky.domain.user.entity.User;
import com.looky.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreNewsService {

    private final StoreNewsRepository storeNewsRepository;
    private final StoreNewsCommentRepository storeNewsCommentRepository;
    private final StoreNewsLikeRepository storeNewsLikeRepository;
    private final StoreRepository storeRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createStoreNews(User user, CreateStoreNewsRequest request, Long storeId, List<MultipartFile> images)
            throws IOException {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        if (!store.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 이미지 유효성 검사 (최대 5장, 10MB)
        FileValidator.validateImageFiles(images, 5, 10 * 1024 * 1024);

        StoreNews storeNews = StoreNews.builder()
                .store(store)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = s3Service.uploadFile(file);
                    storeNews.addImage(StoreNewsImage.builder().imageUrl(imageUrl).build());
                }
            }
        }

        return storeNewsRepository.save(storeNews).getId();
    }

    public PageResponse<StoreNewsResponse> getStoreNewsList(Long storeId, Pageable pageable, User currentUser) {
        Page<StoreNews> page = storeNewsRepository.findByStoreId(storeId, pageable);

        Page<StoreNewsResponse> responsePage = page.map(news -> {
            boolean isLiked = false;
            if (currentUser != null) {
                isLiked = storeNewsLikeRepository.existsByStoreNewsIdAndUserId(news.getId(), currentUser.getId());
            }
            return StoreNewsResponse.from(news, isLiked);
        });

        return PageResponse.from(responsePage);
    }

    public StoreNewsResponse getStoreNews(Long newsId, User currentUser) {
        StoreNews news = storeNewsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게 소식을 찾을 수 없습니다."));

        boolean isLiked = false;

        if (currentUser != null) {
            isLiked = storeNewsLikeRepository.existsByStoreNewsIdAndUserId(newsId, currentUser.getId());
        }
        return StoreNewsResponse.from(news, isLiked);
    }

    @Transactional
    public void updateStoreNews(Long newsId, User user, UpdateStoreNewsRequest request, List<MultipartFile> images)
            throws IOException {
        StoreNews news = storeNewsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게 소식을 찾을 수 없습니다."));

        if (!news.getStore().getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 이미지 유효성 검사 (최대 5장, 10MB)
        if (images != null && !images.isEmpty()) {
            FileValidator.validateImageFiles(images, 5, 10 * 1024 * 1024);
        }

        news.update(request.getTitle(), request.getContent());

        if (images != null) {
            for (StoreNewsImage oldImage : news.getImages()) {
                s3Service.deleteFile(oldImage.getImageUrl());
            }
            news.clearImages();

            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = s3Service.uploadFile(file);
                    news.addImage(StoreNewsImage.builder().imageUrl(imageUrl).build());
                }
            }
        }
    }

    @Transactional
    public void deleteStoreNews(Long newsId, User user) {
        StoreNews news = storeNewsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게 소식을 찾을 수 없습니다."));

        if (!news.getStore().getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        storeNewsRepository.delete(news);
    }

    @Transactional
    public void toggleLike(Long newsId, User user) {
        StoreNews news = storeNewsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게 소식을 찾을 수 없습니다."));

        storeNewsLikeRepository.findByStoreNewsIdAndUserId(newsId, user.getId())
                .ifPresentOrElse(
                        like -> {
                            storeNewsLikeRepository.delete(like);
                            news.decreaseLikeCount();
                        },
                        () -> {
                            storeNewsLikeRepository.save(StoreNewsLike.builder()
                                    .storeNews(news)
                                    .user(user)
                                    .build());
                            news.increaseLikeCount();
                        });
    }

    @Transactional
    public Long createComment(Long newsId, User user, CreateStoreNewsCommentRequest request) {
        StoreNews news = storeNewsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게 소식을 찾을 수 없습니다."));

        StoreNewsComment comment = StoreNewsComment.builder()
                .storeNews(news)
                .user(user)
                .content(request.getContent())
                .build();

        Long commentId = storeNewsCommentRepository.save(comment).getId();
        news.increaseCommentCount();
        return commentId;
    }

    public PageResponse<StoreNewsCommentResponse> getComments(Long newsId, Pageable pageable, User currentUser) {
        Page<StoreNewsComment> page = storeNewsCommentRepository.findByStoreNewsId(newsId, pageable);
        Page<StoreNewsCommentResponse> responsePage = page
                .map(comment -> StoreNewsCommentResponse.from(comment, currentUser));
        return PageResponse.from(responsePage);
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        StoreNewsComment comment = storeNewsCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        comment.getStoreNews().decreaseCommentCount();
        storeNewsCommentRepository.delete(comment);
    }
}
