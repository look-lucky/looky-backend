
package com.looky.domain.item.service;

import com.looky.common.util.FileValidator;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.S3Service;
import com.looky.domain.item.dto.CreateItemRequest;
import com.looky.domain.item.dto.ItemResponse;
import com.looky.domain.item.dto.UpdateItemRequest;
import com.looky.domain.item.entity.Item;
import com.looky.domain.item.entity.ItemCategory;
import com.looky.domain.item.repository.ItemCategoryRepository;
import com.looky.domain.item.repository.ItemRepository;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.store.service.StoreService;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final StoreService storeService;

    @Transactional
    public Long createItem(Long storeId, User user, CreateItemRequest request, MultipartFile image) throws IOException {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, user);

        // 이미지 S3에 업로드
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            // 이미지 유효성 검사 (10MB)
            FileValidator.validateImageFile(image, 10 * 1024 * 1024);
            imageUrl = s3Service.uploadFile(image);
        }

        ItemCategory itemCategory = null;
        if (request.getItemCategoryId() != null) {
            itemCategory = itemCategoryRepository.findById(request.getItemCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "카테고리를 찾을 수 없습니다."));
            
            if (!Objects.equals(itemCategory.getStore().getId(), store.getId())) {
                 throw new CustomException(ErrorCode.BAD_REQUEST, "해당 매장의 카테고리가 아닙니다.");
            }
        }

        Item item = request.toEntity(store, itemCategory, imageUrl);
        Item savedItem = itemRepository.save(item);
        
        // 등급 재계산
        storeService.recalculateCloverGrade(store);
        
        return savedItem.getId();
    }

    public List<ItemResponse> getItems(Long storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        return itemRepository.findByStoreId(storeId).stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    public ItemResponse getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));
        return ItemResponse.from(item);
    }

    @Transactional
    public void updateItem(Long itemId, User user, UpdateItemRequest request, MultipartFile image) throws IOException{
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        validateStoreOwner(item.getStore(), user);

        String imageUrl = item.getImageUrl();

        if (image != null && !image.isEmpty()) {
            // 이미지 유효성 검사 (10MB)
            FileValidator.validateImageFile(image, 10 * 1024 * 1024);
            
            // 기존 이미지 있다면 S3에서 삭제
            if (imageUrl != null && !imageUrl.isEmpty()) {
                s3Service.deleteFile(imageUrl);
            }

            // 새 이미지 업로드 및 URL 교체
            imageUrl = s3Service.uploadFile(image);
        }

        ItemCategory itemCategory = null;

        if (request.getRemoveItemCategory() != null && request.getRemoveItemCategory()) { // 카테고리 삭제
            item.removeItemCategory();
        } else if (request.getItemCategoryId() != null) { // 카테고리 변경
            itemCategory = itemCategoryRepository.findById(request.getItemCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "카테고리를 찾을 수 없습니다."));

            if (!Objects.equals(itemCategory.getStore().getId(), item.getStore().getId())) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "해당 매장의 카테고리가 아닙니다.");
            }
        }

        item.updateItem(
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                imageUrl,
                request.getIsSoldOut(),
                request.getItemOrder(),
                request.getIsRepresentative(),
                request.getIsHidden(),
                request.getBadge(),
                itemCategory
        );
        
        // 등급 재계산
        storeService.recalculateCloverGrade(item.getStore());
    }

    @Transactional
    public void deleteItem(Long itemId, User user) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        validateStoreOwner(item.getStore(), user);

        itemRepository.delete(item);
        
        // 등급 재계산
        storeService.recalculateCloverGrade(item.getStore());
    }

    private void validateStoreOwner(Store store, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "가게 주인이 아닙니다.");
        }
    }
}
