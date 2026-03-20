package com.looky.domain.storenews.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.S3Service;
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
import com.looky.domain.user.repository.OwnerProfileRepository;
import com.looky.domain.user.repository.StudentProfileRepository;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreNewsService {

    private final StoreNewsRepository storeNewsRepository;
    private final StoreNewsCommentRepository storeNewsCommentRepository;
    private final StoreNewsLikeRepository storeNewsLikeRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final S3Service s3Service;

    // --- 점주용 ---

    @Transactional
    public Long createStoreNewsForOwner(Long storeId, User user, CreateStoreNewsRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        validateOwnerStore(store, user);

        List<String> imageUrls = request.getImageUrls();
        if (imageUrls != null && imageUrls.size() > 5) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 5장까지 등록할 수 있습니다.");
        }

        StoreNews storeNews = StoreNews.builder()
                .store(store)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                storeNews.addImage(StoreNewsImage.builder()
                        .imageUrl(imageUrls.get(i))
                        .orderIndex(i)
                        .build());
            }
        }

        return storeNewsRepository.save(storeNews).getId();
    }

    @Transactional
    public void updateStoreNewsForOwner(Long newsId, User user, UpdateStoreNewsRequest request) {
        StoreNews news = storeNewsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게 소식을 찾을 수 없습니다."));

        validateOwnerStore(news.getStore(), user);

        String title = news.getTitle();
        if (request.getTitle().isPresent()) {
            title = request.getTitle().get();
            if (title == null || title.isBlank()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "제목은 필수입니다.");
            }
        }

        String content = news.getContent();
        if (request.getContent().isPresent()) {
            content = request.getContent().get();
            if (content == null || content.isBlank()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "내용은 필수입니다.");
            }
        }

        news.update(title, content);

        // 이미지 처리
        if (request.getImageUrls().isPresent()) {
            List<String> desiredUrls = request.getImageUrls().get() != null
                    ? request.getImageUrls().get() : Collections.emptyList();

            if (desiredUrls.size() > 5) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 5장까지 등록할 수 있습니다.");
            }

            Set<String> desiredSet = new HashSet<>(desiredUrls);

            // desired에 없는 기존 이미지 삭제
            news.getImages().stream()
                    .filter(img -> !desiredSet.contains(img.getImageUrl()))
                    .toList()
                    .forEach(img -> {
                        s3Service.deleteFile(img.getImageUrl());
                        news.removeImage(img);
                    });

            // DB에 없는 새 URL 추가
            Set<String> existingUrls = news.getImages().stream()
                    .map(StoreNewsImage::getImageUrl)
                    .collect(Collectors.toSet());
            for (String url : desiredUrls) {
                if (!existingUrls.contains(url)) {
                    news.addImage(StoreNewsImage.builder()
                            .imageUrl(url)
                            .orderIndex(0)
                            .build());
                }
            }

            // desiredUrls 순서대로 인덱스 재정렬
            Map<String, StoreNewsImage> urlToImage = news.getImages().stream()
                    .collect(Collectors.toMap(StoreNewsImage::getImageUrl, img -> img));
            for (int i = 0; i < desiredUrls.size(); i++) {
                StoreNewsImage img = urlToImage.get(desiredUrls.get(i));
                if (img != null) img.updateOrderIndex(i);
            }
        }
    }

    @Transactional
    public void deleteStoreNewsForOwner(Long newsId, User user) {
        StoreNews news = storeNewsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게 소식을 찾을 수 없습니다."));

        validateOwnerStore(news.getStore(), user);

        storeNewsRepository.delete(news);
    }

    public PageResponse<StoreNewsResponse> getStoreNewsListForOwner(Long storeId, Pageable pageable, User user) {
        return getStoreNewsListInternal(storeId, pageable, user);
    }

    public StoreNewsResponse getStoreNewsForOwner(Long newsId, User user) {
        return getStoreNewsInternal(newsId, user);
    }

    @Transactional
    public void toggleLikeForOwner(Long newsId, User user) {
        toggleLikeInternal(newsId, user);
    }

    @Transactional
    public Long createCommentForOwner(Long newsId, User user, CreateStoreNewsCommentRequest request) {
        return createCommentInternal(newsId, user, request);
    }

    public PageResponse<StoreNewsCommentResponse> getCommentsForOwner(Long newsId, Pageable pageable, User user) {
        return getCommentsInternal(newsId, pageable, user);
    }

    @Transactional
    public void deleteCommentForOwner(Long commentId, User user) {
        deleteCommentInternal(commentId, user);
    }

    // --- 학생용 ---

    public PageResponse<StoreNewsResponse> getStoreNewsListForStudent(Long storeId, Pageable pageable, User user) {
        return getStoreNewsListInternal(storeId, pageable, user);
    }

    public StoreNewsResponse getStoreNewsForStudent(Long newsId, User user) {
        return getStoreNewsInternal(newsId, user);
    }

    @Transactional
    public void toggleLikeForStudent(Long newsId, User user) {
        toggleLikeInternal(newsId, user);
    }

    @Transactional
    public Long createCommentForStudent(Long newsId, User user, CreateStoreNewsCommentRequest request) {
        return createCommentInternal(newsId, user, request);
    }

    public PageResponse<StoreNewsCommentResponse> getCommentsForStudent(Long newsId, Pageable pageable, User user) {
        return getCommentsInternal(newsId, pageable, user);
    }

    @Transactional
    public void deleteCommentForStudent(Long commentId, User user) {
        deleteCommentInternal(commentId, user);
    }

    // -- 내부 메서드 --

    private PageResponse<StoreNewsResponse> getStoreNewsListInternal(Long storeId, Pageable pageable, User currentUser) {
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

    private StoreNewsResponse getStoreNewsInternal(Long newsId, User currentUser) {
        StoreNews news = storeNewsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게 소식을 찾을 수 없습니다."));

        boolean isLiked = false;
        if (currentUser != null) {
            isLiked = storeNewsLikeRepository.existsByStoreNewsIdAndUserId(newsId, currentUser.getId());
        }
        return StoreNewsResponse.from(news, isLiked);
    }

    private void toggleLikeInternal(Long newsId, User user) {
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

    private Long createCommentInternal(Long newsId, User user, CreateStoreNewsCommentRequest request) {
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

    private PageResponse<StoreNewsCommentResponse> getCommentsInternal(Long newsId, Pageable pageable, User currentUser) {
        Page<StoreNewsComment> page = storeNewsCommentRepository.findByStoreNewsId(newsId, pageable);

        List<Long> userIds = page.getContent().stream()
                .map(comment -> comment.getUser().getId())
                .distinct()
                .toList();

        Map<Long, String> nicknameMap = new HashMap<>();

        if (!userIds.isEmpty()) {
            List<com.looky.domain.user.entity.StudentProfile> students = studentProfileRepository.findAllById(userIds);
            students.forEach(p -> nicknameMap.put(p.getUserId(), p.getNickname()));

            List<com.looky.domain.user.entity.OwnerProfile> owners = ownerProfileRepository.findAllById(userIds);
            owners.forEach(p -> nicknameMap.put(p.getUserId(), p.getName()));
        }

        Page<StoreNewsCommentResponse> responsePage = page.map(comment -> {
            String nickname = nicknameMap.getOrDefault(comment.getUser().getId(), "알 수 없음");
            return StoreNewsCommentResponse.from(comment, currentUser, nickname);
        });

        return PageResponse.from(responsePage);
    }

    private void validateOwnerStore(Store store, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private void deleteCommentInternal(Long commentId, User user) {
        StoreNewsComment comment = storeNewsCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        comment.getStoreNews().decreaseCommentCount();
        storeNewsCommentRepository.delete(comment);
    }
}
