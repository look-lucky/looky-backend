package com.looky.domain.advertisement.dto;

import com.looky.domain.advertisement.entity.AdvertisementType;
import com.looky.domain.user.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "광고 등록 요청")
public class CreateAdvertisementRequest {

    @NotBlank(message = "광고 제목은 필수입니다.")
    @Schema(description = "광고 제목", example = "여름 이벤트 팝업 광고")
    private String title;

    @NotNull(message = "광고 타입은 필수입니다.")
    @Schema(description = "광고 타입 (POPUP / BANNER / FLOATING)", example = "POPUP")
    private AdvertisementType advertisementType;

    @NotBlank(message = "이미지 URL은 필수입니다.")
    @Schema(description = "광고 이미지 URL", example = "https://cdn.looky.com/advertisements/popup/image.jpg")
    private String imageUrl;

    @Schema(description = "랜딩 URL (없으면 클릭 불가)", example = "https://event.looky.com/summer", nullable = true)
    private String landingUrl;

    @Min(value = 0, message = "노출 순서는 0 이상이어야 합니다.")
    @Schema(description = "노출 순서 (낮을수록 우선 노출, ACTIVE 시작 시만 적용 / 미입력 시 마지막 순서)", example = "0", nullable = true)
    private Integer displayOrder;

    @NotNull(message = "노출 시작일은 필수입니다.")
    @Schema(description = "노출 시작일시", example = "2026-04-01T00:00:00")
    private LocalDateTime startAt;

    @NotNull(message = "노출 종료일은 필수입니다.")
    @Schema(description = "노출 종료일시", example = "2026-04-30T23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "타겟 대학 ID 목록 (없으면 전체 대학 대상)", nullable = true)
    private List<Long> targetUniversityIds;

    @Schema(description = "타겟 단과대 ID 목록 (없으면 전체 단과대 대상, 반드시 대학 ID도 함께 지정해야 함)", nullable = true)
    private List<Long> targetOrganizationIds;

    @Schema(description = "타겟 성별 (없으면 전체 성별 대상, MALE / FEMALE / UNKNOWN)", nullable = true)
    private Gender targetGender;
}
