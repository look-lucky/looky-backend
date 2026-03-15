package com.looky.domain.store.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.S3Service;
import com.looky.domain.store.dto.UpdateMenuBoardImagesRequest;
import com.looky.domain.store.entity.MenuBoardImage;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuBoardImageService {

    private final StoreRepository storeRepository;
    private final StoreService storeService;
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

        storeService.validateStoreOwner(store, user);

        List<String> imageUrls = request.getImageUrls() != null ? request.getImageUrls() : Collections.emptyList();

        if (imageUrls.size() > 10) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "메뉴판 이미지는 최대 10장까지 등록할 수 있습니다.");
        }

        s3Service.syncImages(
                store::getMenuBoardImages,
                imageUrls,
                store::removeMenuBoardImage,
                url -> MenuBoardImage.builder().imageUrl(url).orderIndex(0).build(),
                store::addMenuBoardImage
        );
    }
}
