package com.neardeal.domain.item.service;

import com.neardeal.common.exception.CustomException;
import com.neardeal.common.exception.ErrorCode;
import com.neardeal.common.service.S3Service;
import com.neardeal.domain.item.dto.CreateItemRequest;
import com.neardeal.domain.item.dto.ItemResponse;
import com.neardeal.domain.item.dto.UpdateItemRequest;
import com.neardeal.domain.item.entity.Item;
import com.neardeal.domain.item.repository.ItemRepository;
import com.neardeal.domain.store.entity.Store;
import com.neardeal.domain.store.repository.StoreRepository;
import com.neardeal.domain.user.entity.User;
import com.neardeal.domain.user.repository.UserRepository;
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
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createItem(Long storeId, User user, CreateItemRequest request, MultipartFile image) throws IOException {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        validateStoreOwner(store, user);

        // 이미지 S3에 업로드
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.uploadFile(image);
        }

        Item item = request.toEntity(store, imageUrl);
        Item savedItem = itemRepository.save(item);
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

            // 기존 이미지 있다면 S3에서 삭제
            if (imageUrl != null && !imageUrl.isEmpty()) {
                s3Service.deleteFile(imageUrl);
            }

            // 새 이미지 업로드 및 URL 교체
            imageUrl = s3Service.uploadFile(image);
        }

        item.updateItem(
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                imageUrl,
                request.getIsSoldOut()
        );
    }

    @Transactional
    public void deleteItem(Long itemId, User user) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        validateStoreOwner(item.getStore(), user);

        itemRepository.delete(item);
    }

    private void validateStoreOwner(Store store, User user) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "가게 주인이 아닙니다.");
        }
    }
}
