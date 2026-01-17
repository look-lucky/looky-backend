package com.neardeal.domain.storenews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "가게 소식 생성 요청")
public class CreateStoreNewsRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "소식 제목", example = "오늘의 할인 이벤트!")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "소식 내용", example = "오늘 하루만 전 메뉴 10% 할인합니다.")
    private String content;

}
