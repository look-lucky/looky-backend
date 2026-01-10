package com.neardeal.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    // 파일 업로드
    public String uploadFile(MultipartFile file) throws IOException {

        // 파일명 중복 방지를 위해 UUID 사용
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // S3에 업로드 요청 객체 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        // 실제 업로드 수행
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        // 업로드된 파일의 URL 반환 (Public 접근이 가능한 경우 유효)
        // 만약 Public이 아니라면 Presigned URL 등을 고려해야 합니다.
        return s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build()).toString();
    }

    // 파일 삭제
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            // URL에서 Key 추출 (예: https://버킷.s3.region.amazonaws.com/파일키.jpg -> 파일키.jpg)
            String splitStr = ".com/";
            String fileName = fileUrl.substring(fileUrl.lastIndexOf(splitStr) + splitStr.length());

            // 한글 파일명 등을 대비해 디코딩
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
}