package com.looky.domain.inquiry.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.S3Service;
import com.looky.domain.inquiry.dto.CreateInquiryRequest;
import com.looky.domain.inquiry.dto.InquiryResponse;
import com.looky.domain.inquiry.entity.Inquiry;
import com.looky.domain.inquiry.entity.InquiryImage;
import com.looky.domain.inquiry.repository.InquiryRepository;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createInquiry(User user, CreateInquiryRequest request, List<MultipartFile> images) throws IOException {
        User writer = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (images != null && images.size() > 5) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 5장까지 첨부 가능합니다.");
        }

        Inquiry inquiry = request.toEntity(writer);
        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        if (images != null) {
            int orderIndex = 0;
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = s3Service.uploadFile(image);
                    InquiryImage inquiryImage = InquiryImage.builder()
                            .inquiry(savedInquiry)
                            .imageUrl(imageUrl)
                            .orderIndex(orderIndex++)
                            .build();
                    savedInquiry.addImage(inquiryImage);
                }
            }
        }

        return savedInquiry.getId();
    }

    public Page<InquiryResponse> getInquiries(User user, Pageable pageable) {
        return inquiryRepository.findByUserId(user.getId(), pageable)
                .map(InquiryResponse::from);
    }

    public InquiryResponse getInquiry(User user, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "문의를 찾을 수 없습니다."));

        if (!inquiry.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인의 문의만 확인할 수 있습니다.");
        }

        return InquiryResponse.from(inquiry);
    }

    public Page<InquiryResponse> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable)
                .map(InquiryResponse::from);
    }
}
