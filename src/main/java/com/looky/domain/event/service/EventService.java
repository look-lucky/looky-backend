package com.looky.domain.event.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.response.PageResponse;
import com.looky.common.service.S3Service;
import com.looky.domain.event.dto.CreateEventRequest;
import com.looky.domain.event.dto.EventResponse;
import com.looky.domain.event.dto.UpdateEventRequest;
import com.looky.domain.event.entity.Event;
import com.looky.domain.event.entity.EventImage;
import com.looky.domain.event.entity.EventImageType;
import com.looky.domain.event.entity.EventStatus;
import com.looky.domain.event.entity.EventType;
import com.looky.domain.event.repository.EventRepository;
import com.looky.domain.event.repository.EventSpecification;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UniversityRepository universityRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createEvent(CreateEventRequest request) {

        University university = null;
        if (request.getUniversityId() != null) {
            university = universityRepository.findById(request.getUniversityId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학교를 찾을 수 없습니다."));
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .subtitle(request.getSubtitle())
                .eventTypes(new HashSet<>(request.getEventTypes()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .place(request.getPlace())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .status(EventStatus.UPCOMING)
                .university(university)
                .build();

        // 배너 이미지
        if (request.getBannerImageUrl() != null) {
            event.addImage(EventImage.builder()
                    .imageUrl(request.getBannerImageUrl())
                    .orderIndex(0)
                    .imageType(EventImageType.BANNER)
                    .build());
        }

        // 일반 이미지
        List<String> imageUrls = request.getImageUrls();
        if (imageUrls != null && imageUrls.size() > 10) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 10장까지 등록할 수 있습니다.");
        }
        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                event.addImage(EventImage.builder()
                        .imageUrl(imageUrls.get(i))
                        .orderIndex(i)
                        .imageType(EventImageType.GENERAL)
                        .build());
            }
        }

        Event savedEvent = eventRepository.save(event);
        return savedEvent.getId();
    }

    public EventResponse getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        return EventResponse.from(event);
    }

    public PageResponse<EventResponse> getEvents(String keyword, List<EventType> eventTypes, EventStatus status, Long universityId, Pageable pageable) {
        Specification<Event> spec = Specification.where(EventSpecification.hasKeyword(keyword))
                .and(EventSpecification.hasEventTypes(eventTypes))
                .and(EventSpecification.hasStatus(status))
                .and(EventSpecification.hasUniversityId(universityId));

        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        Page<EventResponse> responsePage = eventPage.map(EventResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional
    public void updateEvent(Long eventId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        event.update(
                request.getTitle().orElse(event.getTitle()),
                request.getDescription().orElse(event.getDescription()),
                request.getSubtitle().orElse(event.getSubtitle()),
                request.getEventTypes().isPresent() ? (request.getEventTypes().get() != null ? new HashSet<>(request.getEventTypes().get()) : new HashSet<>()) : null,
                request.getLatitude().orElse(event.getLatitude()),
                request.getLongitude().orElse(event.getLongitude()),
                request.getPlace().orElse(event.getPlace()),
                request.getStartDateTime().orElse(event.getStartDateTime()),
                request.getEndDateTime().orElse(event.getEndDateTime()),
                request.getStatus().orElse(event.getStatus()),
                request.getUniversityId().isPresent() ?
                        (request.getUniversityId().get() != null ?
                                universityRepository.findById(request.getUniversityId().get())
                                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학교를 찾을 수 없습니다."))
                                : null)
                        : event.getUniversity()
        );

        // 배너 이미지 처리
        if (request.getBannerImageUrl().isPresent()) {
            String newBannerUrl = request.getBannerImageUrl().get();
            String currentBannerUrl = event.getImages().stream()
                    .filter(img -> img.getImageType() == EventImageType.BANNER)
                    .map(EventImage::getImageUrl)
                    .findFirst().orElse(null);

            if (!Objects.equals(currentBannerUrl, newBannerUrl)) {
                // 기존 배너 S3 삭제 + DB 제거
                event.getImages().stream()
                        .filter(img -> img.getImageType() == EventImageType.BANNER)
                        .toList()
                        .forEach(img -> {
                            s3Service.deleteFile(img.getImageUrl());
                            event.getImages().remove(img);
                            img.setEvent(null);
                        });
                // 새 배너 추가 (null이면 삭제만)
                if (newBannerUrl != null) {
                    event.addImage(EventImage.builder()
                            .imageUrl(newBannerUrl)
                            .orderIndex(0)
                            .imageType(EventImageType.BANNER)
                            .build());
                }
            }
        }

        // 일반 이미지 처리
        if (request.getImageUrls().isPresent()) {
            List<String> desiredUrls = request.getImageUrls().get() != null
                    ? request.getImageUrls().get() : Collections.emptyList();

            if (desiredUrls.size() > 10) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 10장까지 등록할 수 있습니다.");
            }

            Set<String> desiredSet = new HashSet<>(desiredUrls);

            // desired에 없는 GENERAL 이미지 삭제
            event.getImages().stream()
                    .filter(img -> img.getImageType() == EventImageType.GENERAL)
                    .filter(img -> !desiredSet.contains(img.getImageUrl()))
                    .toList()
                    .forEach(img -> {
                        s3Service.deleteFile(img.getImageUrl());
                        event.getImages().remove(img);
                        img.setEvent(null);
                    });

            // DB에 없는 새 URL 추가
            Set<String> existingGeneralUrls = event.getImages().stream()
                    .filter(img -> img.getImageType() == EventImageType.GENERAL)
                    .map(EventImage::getImageUrl)
                    .collect(Collectors.toSet());
            for (String url : desiredUrls) {
                if (!existingGeneralUrls.contains(url)) {
                    event.addImage(EventImage.builder()
                            .imageUrl(url)
                            .orderIndex(0)
                            .imageType(EventImageType.GENERAL)
                            .build());
                }
            }

            // desiredUrls 순서대로 인덱스 재정렬
            Map<String, EventImage> urlToImage = event.getImages().stream()
                    .filter(img -> img.getImageType() == EventImageType.GENERAL)
                    .collect(Collectors.toMap(EventImage::getImageUrl, img -> img));
            for (int i = 0; i < desiredUrls.size(); i++) {
                EventImage img = urlToImage.get(desiredUrls.get(i));
                if (img != null) img.updateOrderIndex(i);
            }
        }
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        for (EventImage image : event.getImages()) {
            s3Service.deleteFile(image.getImageUrl());
        }

        eventRepository.delete(event);
    }
}
