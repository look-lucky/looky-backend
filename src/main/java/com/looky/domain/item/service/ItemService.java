package com.looky.domain.item.service;

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
    public Long createItem(Long storeId, User user, CreateItemRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, user);

        ItemCategory itemCategory = null;
        if (request.getItemCategoryId() != null) {
            itemCategory = itemCategoryRepository.findById(request.getItemCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "카테고리를 찾을 수 없습니다."));

            if (!Objects.equals(itemCategory.getStore().getId(), store.getId())) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "해당 매장의 카테고리가 아닙니다.");
            }
        }

        Item item = request.toEntity(store, itemCategory, request.getImageUrl());
        Item savedItem = itemRepository.save(item);

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
    public void updateItem(Long itemId, User user, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        validateStoreOwner(item.getStore(), user);

        if (request.getName().isPresent() && request.getName().get() == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "상품명은 필수입니다.");
        }
        if (request.getPrice().isPresent() && request.getPrice().get() == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "가격은 필수입니다.");
        }

        // 이미지 처리
        String imageUrl = item.getImageUrl();
        if (request.getImageUrl().isPresent()) {
            String newImageUrl = request.getImageUrl().get();
            if (!Objects.equals(imageUrl, newImageUrl)) {
                if (imageUrl != null) {
                    s3Service.deleteFile(imageUrl);
                }
                imageUrl = newImageUrl; // null(삭제) 또는 새 URL(교체)
            }
        }

        ItemCategory itemCategory = item.getItemCategory();

        if (request.getItemCategoryId().isPresent()) {
            Long newCategoryId = request.getItemCategoryId().get();
            if (newCategoryId == null) {
                itemCategory = null;
            } else {
                ItemCategory newCategory = itemCategoryRepository.findById(newCategoryId)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "카테고리를 찾을 수 없습니다."));

                if (!Objects.equals(newCategory.getStore().getId(), item.getStore().getId())) {
                    throw new CustomException(ErrorCode.BAD_REQUEST, "해당 매장의 카테고리가 아닙니다.");
                }
                itemCategory = newCategory;
            }
        }

        item.updateItem(
                request.getName().orElse(item.getName()),
                request.getPrice().orElse(item.getPrice()),
                request.getDescription().orElse(item.getDescription()),
                imageUrl,
                request.getIsSoldOut().orElse(item.getIsSoldOut()),
                request.getItemOrder().orElse(item.getItemOrder()),
                request.getIsRepresentative().orElse(item.getIsRepresentative()),
                request.getIsHidden().orElse(item.getIsHidden()),
                request.getBadge().orElse(item.getBadge()),
                itemCategory
        );

        storeService.recalculateCloverGrade(item.getStore());
    }

    @Transactional
    public void deleteItem(Long itemId, User user) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        validateStoreOwner(item.getStore(), user);

        itemRepository.delete(item);

        storeService.recalculateCloverGrade(item.getStore());
    }

    private void validateStoreOwner(Store store, User user) {
        storeService.validateStoreOwner(store, user);
    }
}
