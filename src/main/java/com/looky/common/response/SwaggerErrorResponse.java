package com.looky.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답")
public class SwaggerErrorResponse extends CommonResponse<ErrorResponse> {

    @Schema(description = "성공 여부", example = "false")
    @Override
    public Boolean getIsSuccess() {
        return false;
    }

    private SwaggerErrorResponse(ErrorResponse data) {
        super(false, data);
    }
}
