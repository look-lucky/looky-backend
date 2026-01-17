package com.neardeal.domain.storenews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "소식 댓글 생성 요청")
public class CreateStoreNewsCommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하여야 합니다.")
    @Schema(description = "댓글 내용", example = "정말 좋은 이벤트네요!")
    private String content;
}
