package com.looky.domain.inquiry.dto;

import com.looky.domain.inquiry.entity.Inquiry;
import com.looky.domain.inquiry.entity.InquiryType;
import com.looky.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInquiryRequest {

    @NotNull(message = "문의 유형을 선택해주세요.")
    private InquiryType type;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 1, max = 14, message = "제목은 1자 이상 14자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 500, message = "내용은 500자 이하로 입력해주세요.")
    private String content;

    public Inquiry toEntity(User user) {
        return Inquiry.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .build();
    }
}
