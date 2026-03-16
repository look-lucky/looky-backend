package com.looky.common.service;

import com.looky.common.entity.OrderedImage;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    /**
     * 이미지 목록을 desired 상태로 동기화합니다 (삭제 / 추가 / 순서 재정렬).
     * S3에서 제거된 이미지는 자동으로 삭제됩니다.
     *
     * @param getImages    현재 이미지 컬렉션을 반환하는 Supplier (삭제 후 재조회를 위해 Supplier 사용)
     * @param desiredUrls  최종적으로 유지할 URL 목록 (순서 포함)
     * @param removeAction 이미지를 엔티티에서 제거하는 동작
     * @param createAction URL로 이미지 엔티티를 생성하는 팩토리
     * @param addAction    생성된 이미지를 엔티티에 추가하는 동작
     */
    public <T extends OrderedImage> void syncImages(
            Supplier<List<T>> getImages,
            List<String> desiredUrls,
            Consumer<T> removeAction,
            Function<String, T> createAction,
            Consumer<T> addAction) {

        Set<String> desiredSet = new HashSet<>(desiredUrls);

        getImages.get().stream()
                .filter(img -> !desiredSet.contains(img.getImageUrl()))
                .toList()
                .forEach(img -> {
                    deleteFile(img.getImageUrl());
                    removeAction.accept(img);
                });

        Set<String> existingUrls = getImages.get().stream()
                .map(OrderedImage::getImageUrl)
                .collect(Collectors.toSet());
        for (String url : desiredUrls) {
            if (!existingUrls.contains(url)) {
                addAction.accept(createAction.apply(url));
            }
        }

        Map<String, T> urlToImage = getImages.get().stream()
                .collect(Collectors.toMap(OrderedImage::getImageUrl, img -> img));
        for (int i = 0; i < desiredUrls.size(); i++) {
            T img = urlToImage.get(desiredUrls.get(i));
            if (img != null) img.updateOrderIndex(i);
        }
    }

    public record PresignedUrlResult(String presignedUrl, String fileUrl) {}
}
