package com.looky.domain.storeclaim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BizVerificationRequest {

    @JsonProperty("bizs")
    private List<BizInfo> bizs;

    @Getter
    @Builder
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class BizInfo {
        @JsonProperty("b_no")
        private String bNo; // 사업자등록번호

        @JsonProperty("start_dt")
        private String startDt; // 개업일자 (YYYYMMDD)

        @JsonProperty("p_nm")
        private String pNm; // 대표자 성명
    }
}
