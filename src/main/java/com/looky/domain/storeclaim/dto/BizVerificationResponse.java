package com.looky.domain.storeclaim.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BizVerificationResponse {

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("match_cnt")
    private Integer matchCnt;

    @JsonProperty("request_cnt")
    private Integer requestCnt;

    private List<BizStatus> data;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BizStatus {

        @JsonProperty("b_no")
        private String bNo;

        @JsonProperty("valid")
        private String valid;

        @JsonProperty("valid_msg")
        private String validMsg;

        @JsonProperty("status")
        private Status status;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Status {

            @JsonProperty("b_no")
            private String bNo;

            @JsonProperty("b_stt")
            private String bStt;

            @JsonProperty("b_stt_cd")
            private String bSttCd;
        }
    }
}
