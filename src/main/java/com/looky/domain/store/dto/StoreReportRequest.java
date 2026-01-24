package com.looky.domain.store.dto;

import com.looky.domain.store.entity.StoreReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreReportRequest {

    @NotNull(message = "신고 사유는 필수 사항입니다.")
    private List<StoreReportReason> reasons;

    private String detail;
}
