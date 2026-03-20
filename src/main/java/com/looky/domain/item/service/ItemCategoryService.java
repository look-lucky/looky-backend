package com.looky.domain.item.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.item.entity.ItemCategory;
import com.looky.domain.item.dto.ItemCategoryResponse;
import com.looky.domain.item.repository.ItemCategoryRepository;
import com.looky.domain.item.repository.ItemRepository;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreStatus;
import com.looky.domain.store.repository.StoreRepository;
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
public class ItemCategoryService {

    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // --- 점주용 ---

    @Transactional
    public Long createItemCategoryForOwner(Long storeId, User user, String name) {
        Store store = validateOwnerStore(storeId, user);
        return createItemCategoryInternal(store, name);
    }

    public List<ItemCategoryResponse> getItemCategoriesForOwner(Long storeId) {
        return getItemCategoriesInternal(storeId);
    }

    @Transactional
    public void updateItemCategoryForOwner(Long storeId, Long categoryId, User user, String name) {
        Store store = validateOwnerStore(storeId, user);
        updateItemCategoryInternal(store, categoryId, name);
    }

    @Transactional
    public void deleteItemCategoryForOwner(Long storeId, Long categoryId, User user) {
        Store store = validateOwnerStore(storeId, user);
        deleteItemCategoryInternal(store, categoryId);
    }

    // --- 학생용 ---

    public List<ItemCategoryResponse> getItemCategoriesForStudent(Long storeId) {
        return getItemCategoriesInternal(storeId);
    }

    // --- 관리자용 ---

    @Transactional
    public Long createItemCategoryForAdmin(Long storeId, User user, String name) {
        Store store = validateUnclaimedStore(storeId, user);
        return createItemCategoryInternal(store, name);
    }

    public List<ItemCategoryResponse> getItemCategoriesForAdmin(Long storeId) {
        return getItemCategoriesInternal(storeId);
    }

    @Transactional
    public void updateItemCategoryForAdmin(Long storeId, Long categoryId, User user, String name) {
        Store store = validateUnclaimedStore(storeId, user);
        updateItemCategoryInternal(store, categoryId, name);
    }

    @Transactional
    public void deleteItemCategoryForAdmin(Long storeId, Long categoryId, User user) {
        Store store = validateUnclaimedStore(storeId, user);
        deleteItemCategoryInternal(store, categoryId);
    }

    // -- 내부 메서드 --

    private Long createItemCategoryInternal(Store store, String name) {
        ItemCategory itemCategory = ItemCategory.builder()
                .store(store)
                .name(name)
                .build();
        return itemCategoryRepository.save(itemCategory).getId();
    }

    private List<ItemCategoryResponse> getItemCategoriesInternal(Long storeId) {
        return itemCategoryRepository.findByStoreId(storeId).stream()
                .map(ItemCategoryResponse::from)
                .collect(Collectors.toList());
    }

    private void updateItemCategoryInternal(Store store, Long categoryId, String name) {
        ItemCategory category = itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        if (!Objects.equals(category.getStore().getId(), store.getId())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "해당 매장의 카테고리가 아닙니다.");
        }

        if (name == null || name.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "카테고리 이름은 필수입니다.");
        }

        category.updateName(name);
    }

    private void deleteItemCategoryInternal(Store store, Long categoryId) {
        ItemCategory category = itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        if (!Objects.equals(category.getStore().getId(), store.getId())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "해당 매장의 카테고리가 아닙니다.");
        }

        if (itemRepository.existsByItemCategory(category)) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "해당 카테고리를 사용하는 상품이 존재하여 삭제할 수 없습니다.");
        }

        itemCategoryRepository.delete(category);
    }

    // 가게 주인인지 검증 (owner이 사용)
    private Store validateOwnerStore(Long storeId, User user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "매장을 찾을 수 없습니다."));

        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 매장이 아닙니다.");
        }
        return store;
    }

    // unclaimed 가게인지 검증 (admin이 사용)
    private Store validateUnclaimedStore(Long storeId, User user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "매장을 찾을 수 없습니다."));

        User admin = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (store.getStoreStatus() != StoreStatus.UNCLAIMED || admin.getRole() != Role.ROLE_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN, "UNCLAIMED 상태의 매장에만 접근 가능합니다.");
        }
        return store;
    }
}
