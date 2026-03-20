package com.looky.domain.item.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.item.dto.ItemCategoryResponse;
import com.looky.domain.item.service.ItemCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student ItemCategory", description = "학생 상품 카테고리 조회 API")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentItemCategoryController {

    private final ItemCategoryService itemCategoryService;

    @Operation(summary = "[학생] 상품 카테고리 목록 조회", description = "매장의 상품 카테고리 목록을 조회합니다.")
    @GetMapping("/stores/{storeId}/item-categories")
    public ResponseEntity<CommonResponse<List<ItemCategoryResponse>>> getItemCategories(
            @PathVariable Long storeId
    ) {
        List<ItemCategoryResponse> categories = itemCategoryService.getItemCategoriesForStudent(storeId);
        return ResponseEntity.ok(CommonResponse.success(categories));
    }
}
