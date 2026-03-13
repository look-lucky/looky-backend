package com.looky.domain.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "메뉴판 이미지 수정 요청")
public class UpdateMenuBoardImagesRequest {

    @NotNull(message = "이미지 목록은 필수입니다.")
    @Schema(description = "메뉴판 이미지 URL 목록 (최대 10장, 빈 배열 전송 시 전체 삭제)")
    private List<String> imageUrls;
}
