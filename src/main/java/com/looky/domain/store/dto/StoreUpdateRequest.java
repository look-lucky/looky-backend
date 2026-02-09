package com.looky.domain.store.dto;

import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreMood;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@NoArgsConstructor
@Schema(description = "가게 정보 수정 요청")
public class StoreUpdateRequest {

    @Schema(description = "상호명", example = "루키 카페 (수정)")
    private JsonNullable<String> name = JsonNullable.undefined();

    @Schema(description = "지점명", example = "강남점")
    private JsonNullable<String> branch = JsonNullable.undefined();

    @Schema(description = "도로명 주소", example = "서울시 강남구 테헤란로 123")
    private JsonNullable<String> roadAddress = JsonNullable.undefined();

    @Schema(description = "지번 주소", example = "서울시 강남구 역삼동 123-45")
    private JsonNullable<String> jibunAddress = JsonNullable.undefined();

    @Schema(description = "위도", example = "37.123456")
    private JsonNullable<Double> latitude = JsonNullable.undefined();

    @Schema(description = "경도", example = "127.123456")
    private JsonNullable<Double> longitude = JsonNullable.undefined();

    @Schema(description = "가게 전화번호", example = "02-1234-5678")
    private JsonNullable<String> phone = JsonNullable.undefined();

    @Schema(description = "가게 소개", example = "맛있는 커피와 디저트가 있는 공간입니다.")
    private JsonNullable<String> introduction = JsonNullable.undefined();

    @Schema(description = "영업 시간 (JSON 형식 권장)", example = "{\"mon\": \"09:00-22:00\", \"tue\": \"CLOSED\"}")
    private JsonNullable<String> operatingHours = JsonNullable.undefined();

    @Schema(description = "가게 카테고리 목록")
    private JsonNullable<List<StoreCategory>> storeCategories = JsonNullable.undefined();

    @Schema(description = "가게 분위기 목록")
    private JsonNullable<List<StoreMood>> storeMoods = JsonNullable.undefined();

    @Schema(description = "휴무 시작일")
    private JsonNullable<LocalDate> holidayStartsAt = JsonNullable.undefined();

    @Schema(description = "휴무 종료일")
    private JsonNullable<LocalDate> holidayEndsAt = JsonNullable.undefined();

    @Schema(description = "영업 중지 여부", example = "false")
    private JsonNullable<Boolean> isSuspended = JsonNullable.undefined();
}

