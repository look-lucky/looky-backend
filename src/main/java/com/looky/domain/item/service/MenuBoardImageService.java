package com.looky.domain.item.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.S3Service;
import com.looky.domain.item.dto.UpdateMenuBoardImagesRequest;
import com.looky.domain.store.entity.MenuBoardImage;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreStatus;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuBoardImageService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public List<String> getMenuBoardImages(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        return store.getMenuBoardImages().stream()
                .map(MenuBoardImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMenuBoardImages(Long storeId, User user, UpdateMenuBoardImagesRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, user);

        List<String> imageUrls = request.getImageUrls() != null ? request.getImageUrls() : Collections.emptyList();

        if (imageUrls.size() > 10) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "메뉴판 이미지는 최대 10장까지 등록할 수 있습니다.");
        }

        Set<String> desiredSet = new HashSet<>(imageUrls);

        // desired에 없는 기존 이미지 삭제
        store.getMenuBoardImages().stream()
                .filter(img -> !desiredSet.contains(img.getImageUrl()))
                .toList()
                .forEach(img -> {
                    s3Service.deleteFile(img.getImageUrl());
                    store.removeMenuBoardImage(img);
                });

        // DB에 없는 새 URL 추가
        Set<String> existingUrls = store.getMenuBoardImages().stream()
                .map(MenuBoardImage::getImageUrl)
                .collect(Collectors.toSet());
        for (String url : imageUrls) {
            if (!existingUrls.contains(url)) {
                store.addMenuBoardImage(MenuBoardImage.builder()
                        .imageUrl(url)
                        .orderIndex(0)
                        .build());
            }
        }

        // desiredUrls 순서대로 인덱스 재정렬
        Map<String, MenuBoardImage> urlToImage = store.getMenuBoardImages().stream()
                .collect(Collectors.toMap(MenuBoardImage::getImageUrl, img -> img));
        for (int i = 0; i < imageUrls.size(); i++) {
            MenuBoardImage img = urlToImage.get(imageUrls.get(i));
            if (img != null) img.updateOrderIndex(i);
        }
    }

    private void validateStoreOwner(Store store, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (store.getStoreStatus() == StoreStatus.UNCLAIMED && owner.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "가게 주인이 아닙니다.");
        }
    }
}