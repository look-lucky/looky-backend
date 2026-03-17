package com.looky.common.controller;

import com.looky.common.dto.PresignedUrlRequest;
import com.looky.common.dto.PresignedUrlResponse;
import com.looky.common.response.CommonResponse;
import com.looky.common.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Upload", description = "파일 업로드 API")
@RestController
@RequestMapping("/api/presigned-url")
@RequiredArgsConstructor
public class PresignedUrlController {

    private final S3Service s3Service;

    @Operation(
            summary = "Presigned URL 발급",
            description = "S3에 직접 업로드하기 위한 Presigned PUT URL을 발급합니다. 발급된 presignedUrl로 PUT 요청을 보내 파일을 업로드하고, 업로드 완료 후 fileUrl을 각 API에 전달하세요. 유효 시간은 10분입니다. S3 PUT 요청 시 Content-Type 헤더를 요청한 contentType과 동일하게 설정해야 합니다."
    )
    @PostMapping
    public ResponseEntity<CommonResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestBody @Valid PresignedUrlRequest request
    ) {
        S3Service.PresignedUrlResult result = s3Service.generatePresignedUrl(
                request.getFileName(), request.getContentType()
        );
        return ResponseEntity.ok(CommonResponse.success(
                new PresignedUrlResponse(result.presignedUrl(), result.fileUrl())
        ));
    }
}
