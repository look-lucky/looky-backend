package com.looky.domain.store.dto;

import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreMood;
import com.looky.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "가게 정보 생성 요청")
public class StoreCreateRequest {

    @NotBlank(message = "가게 이름은 필수입니다.")
    @Schema(description = "상호명", example = "루키 카페", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "지점명", example = "강남점")
    private String branch;

    @Schema(description = "사업자등록번호", example = "123-45-67890")
    private String bizRegNo;

    @NotBlank(message = "도로명 주소는 필수입니다.")
    @Schema(description = "도로명 주소", example = "서울시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roadAddress;

    @Schema(description = "지번 주소", example = "서울시 강남구 역삼동 123-45")
    private String jibunAddress;

    @Schema(description = "위도", example = "37.123456")
    private Double latitude;

    @Schema(description = "경도", example = "127.123456")
    private Double longitude;

    @Schema(description = "가게 전화번호", example = "02-1234-5678")
    private String storePhone;

    @Schema(description = "대표자명", example = "홍길동")
    private String representativeName;

    @Schema(description = "가게 소개", example = "맛있는 커피와 디저트가 있는 공간입니다.")
    private String introduction;

    @Schema(description = "영업 시간 (JSON 형식 권장)", example = "{\"mon\": \"09:00-22:00\", \"tue\": \"CLOSED\"}")
    private String operatingHours;

    @Schema(description = "가게 카테고리 목록")
    private List<StoreCategory> storeCategories;

    @Schema(description = "가게 분위기 목록")
    private List<StoreMood> storeMoods;

    @Schema(description = "연결할 대학 ID 목록")
    private List<Long> universityIds;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "갤러리 이미지 URL 목록 (최대 3장)")
    private List<String> imageUrls;

    @Schema(description = "메뉴판 이미지 URL 목록 (최대 10장)")
    private List<String> menuBoardImageUrls;

    public Store toEntity(User user) {
        return Store.builder()
                .user(user)
                .name(name)
                .branch(sanitizeBranch(branch))
                .bizRegNo(sanitizeText(bizRegNo))
                .roadAddress(roadAddress)
                .jibunAddress(jibunAddress)
                .latitude(latitude)
                .longitude(longitude)
                .storePhone(storePhone)
                .representativeName(representativeName)
                .introduction(introduction)
                .operatingHours(operatingHours)
                .storeCategories(storeCategories != null ? new HashSet<>(storeCategories) : new HashSet<>())
                .storeMoods(storeMoods != null ? new HashSet<>(storeMoods) : new HashSet<>())
                .build();
    }

    private static String sanitizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String sanitizeBranch(String value) {
        return sanitizeText(value);
    }
}
