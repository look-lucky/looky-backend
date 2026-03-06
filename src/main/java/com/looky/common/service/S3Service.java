package com.looky.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 프론트엔드가 S3에 직접 업로드할 수 있도록 Presigned PUT URL을 생성합니다.
     *
     * @param originalFileName 클라이언트가 업로드할 파일의 원본 이름 (UUID prefix 자동 부여)
     * @param contentType      파일의 MIME 타입 (예: image/jpeg)
     * @return presignedUrl (S3에 직접 PUT 요청할 URL) + fileUrl (업로드 완료 후 접근 가능한 최종 URL)
     */
    public PresignedUrlResult generatePresignedUrl(String originalFileName, String contentType) {
        String key = UUID.randomUUID() + "_" + originalFileName;

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(req -> req
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType))
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        String fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()).toString();

        return new PresignedUrlResult(presignedUrl, fileUrl);
    }

    /**
     * S3에서 파일을 삭제합니다.
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String splitStr = ".com/";
            String fileName = fileUrl.substring(fileUrl.lastIndexOf(splitStr) + splitStr.length());
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(decodedFileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            System.err.println("S3 파일 삭제 실패: " + e.getMessage());
        }
    }

    public record PresignedUrlResult(String presignedUrl, String fileUrl) {}
}
