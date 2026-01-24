package com.looky.domain.review.dto;

import com.looky.domain.review.entity.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {

    @NotNull(message = "신고 사유는 필수 선택 사항입니다.")
    ReportReason reason;

    @Size(max = 300, message = "상세 사유는 300자 이내로 입력해주세요.")
    String detail;

}
