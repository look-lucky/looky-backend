package com.looky.domain.store.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 정의되지 않은 필드 무시
public class BusinessVerificationResponse {

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("match_cnt")
    private Integer matchCnt;

    @JsonProperty("request_cnt")
    private Integer requestCnt;

    private List<BusinessStatus> data;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BusinessStatus {

        // 사업자 등록 번호 (입력한 번호)
        @JsonProperty("b_no")
        private String bNo;

        // 유효 여부 (01 -> 유효, 02 -> 유효 X)
        @JsonProperty("valid")
        private String valid;

        // 결과 메시지
        @JsonProperty("valid_msg")
        private String validMsg;

        // 상태 정보 객체
        @JsonProperty("status")
        private Status status;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Status {

            // 사업자 등록 번호
            @JsonProperty("b_no")
            private String bNo;

            // 사업자 상태 (계속사업자, 휴업자, 폐업자)
            @JsonProperty("b_stt")
            private String bStt;

            // 사업자 상태 코드 (01: 계속사업자, 02: 휴업자, 03: 폐업자)
            @JsonProperty("b_stt_cd")
            private String bSttCd;
        }
    }
}
