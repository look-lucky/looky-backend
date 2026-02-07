package com.looky.common.util;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;

public class FileValidator {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");

    public static void validateImageFile(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new CustomException(ErrorCode.INVALID_FILE_FORMAT, "JPG, PNG 형식만 가능합니다.");
        }

        if (file.getSize() > maxSize) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED, "파일 크기는 " + (maxSize / (1024 * 1024)) + "MB 이하여야 합니다.");
        }
    }

    public static void validateImageFiles(List<MultipartFile> files, int maxCount, long maxSize) {
        if (files == null || files.isEmpty()) {
            return;
        }

        if (files.size() > maxCount) {
            throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED, "이미지는 최대 " + maxCount + "장까지 업로드 가능합니다.");
        }

        for (MultipartFile file : files) {
            validateImageFile(file, maxSize);
        }
    }
}
