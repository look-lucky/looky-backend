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
import com.looky.domain.store.entity.StoreStatus;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.store.service.StoreService;
import com.looky.domain.user.entity.Role;
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

    // --- 점주용 ---

    // 상품 등록
    @Transactional
    public Long createItemForOwner(Long storeId, User user, CreateItemRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));
        validateOwnerStore(store, user);
        return createItemInternal(store, request);
    }

    // 상품 목록 조회
    public List<ItemResponse> getItemsForOwner(Long storeId) {
        return getItemsInternal(storeId);
    }

    // 상품 개별 조회
    public ItemResponse getItemForOwner(Long itemId) {
        return getItemInternal(itemId);
    }

    // 상품 수정
    @Transactional
    public void updateItemForOwner(Long itemId, User user, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));
        validateOwnerStore(item.getStore(), user);
        updateItemInternal(item, request);
    }

    // 상품 삭제
    @Transactional
    public void deleteItemForOwner(Long itemId, User user) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));
        validateOwnerStore(item.getStore(), user);
        deleteItemInternal(item);
    }

    // --- 학생용 ---

    // 상품 목록 조회
    public List<ItemResponse> getItemsForStudent(Long storeId) {
        return getItemsInternal(storeId);
    }

    // 상품 삭제
    public ItemResponse getItemForStudent(Long itemId) {
        return getItemInternal(itemId);
    }

    // --- 관리자용 ---

    @Transactional
    public Long createItemForAdmin(Long storeId, User user, CreateItemRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));
        validateUnclaimedStore(store, user);
        return createItemInternal(store, request);
    }

    public List<ItemResponse> getItemsForAdmin(Long storeId) {
        return getItemsInternal(storeId);
    }

    public ItemResponse getItemForAdmin(Long itemId) {
        return getItemInternal(itemId);
    }

    @Transactional
    public void updateItemForAdmin(Long itemId, User user, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));
        validateUnclaimedStore(item.getStore(), user);
        updateItemInternal(item, request);
    }

    @Transactional
    public void deleteItemForAdmin(Long itemId, User user) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));
        validateUnclaimedStore(item.getStore(), user);
        deleteItemInternal(item);
    }

    // -- 내부 메서드 --

    private Long createItemInternal(Store store, CreateItemRequest request) {
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

    private List<ItemResponse> getItemsInternal(Long storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        return itemRepository.findByStoreId(storeId).stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    private ItemResponse getItemInternal(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));
        return ItemResponse.from(item);
    }

    private void updateItemInternal(Item item, UpdateItemRequest request) {
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

    private void deleteItemInternal(Item item) {
        Store store = item.getStore();
        itemRepository.delete(item);
        storeService.recalculateCloverGrade(store);
    }

    // 가게 주인인지 검증
    private void validateOwnerStore(Store store, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "가게 주인이 아닙니다.");
        }
    }

    // UNCLAIMED 매장인지 검증
    private void validateUnclaimedStore(Store store, User user) {
        User admin = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (store.getStoreStatus() != StoreStatus.UNCLAIMED || admin.getRole() != Role.ROLE_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN, "UNCLAIMED 상태의 매장에만 접근 가능합니다.");
        }
    }
}
