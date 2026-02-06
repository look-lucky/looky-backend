package com.looky.domain.inquiry.dto;

import com.looky.domain.inquiry.entity.Inquiry;
import com.looky.domain.inquiry.entity.InquiryImage;
import com.looky.domain.inquiry.entity.InquiryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class InquiryResponse {
    private Long id;
    private Long userId;
    private String username;
    private InquiryType type;
    private String title;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static InquiryResponse from(Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .userId(inquiry.getUser().getId())
                .username(inquiry.getUser().getUsername()) // User entity uses username
                .type(inquiry.getType())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .imageUrls(inquiry.getImages().stream()
                        .map(InquiryImage::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
