package com.looky.domain.item.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.item.entity.ItemCategory;
import com.looky.domain.item.dto.ItemCategoryResponse;
import com.looky.domain.item.repository.ItemCategoryRepository;
import com.looky.domain.item.repository.ItemRepository;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
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

    @Transactional
    public Long createItemCategory(Long storeId, User user, String name) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        if (!store.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        
        ItemCategory itemCategory = ItemCategory.builder()
                .store(store)
                .name(name)
                .build();
        
        return itemCategoryRepository.save(itemCategory).getId();
    }

    public List<ItemCategoryResponse> getItemCategories(Long storeId) {
        return itemCategoryRepository.findByStoreId(storeId).stream()
                .map(ItemCategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateItemCategory(Long storeId, Long categoryId, User user, String name) {
        getStoreAndValidateOwner(storeId, user);
        
        ItemCategory category = itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "카테고리를 찾을 수 없습니다."));
        
        if (!Objects.equals(category.getStore().getId(), storeId)) {
             throw new CustomException(ErrorCode.BAD_REQUEST, "해당 매장의 카테고리가 아닙니다.");
        }
        
        if (name == null || name.isBlank()) {
             throw new CustomException(ErrorCode.BAD_REQUEST, "카테고리 이름은 필수입니다.");
        }
        
        category.updateName(name);
    }

    @Transactional
    public void deleteItemCategory(Long storeId, Long categoryId, User user) {
        getStoreAndValidateOwner(storeId, user);

        ItemCategory category = itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        if (!Objects.equals(category.getStore().getId(), storeId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "해당 매장의 카테고리가 아닙니다.");
        }

        // 해당 카테고리를 참조하는 아이템이 있는지 확인
        if (itemRepository.existsByItemCategory(category)) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "해당 카테고리를 사용하는 상품이 존재하여 삭제할 수 없습니다.");
        }

        itemCategoryRepository.delete(category);
    }

    private Store getStoreAndValidateOwner(Long storeId, User user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "매장을 찾을 수 없습니다."));

        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
             throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 매장이 아닙니다.");
        }
        return store;
    }
}