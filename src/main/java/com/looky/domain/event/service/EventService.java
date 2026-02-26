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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UniversityRepository universityRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createEvent(CreateEventRequest request, List<MultipartFile> images) throws IOException {
        
        University university = null;
        if (request.getUniversityId() != null && request.getUniversityId() != 0L) {
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

        // 이미지 업로드 및 저장
        uploadAndSaveImages(event, images);

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
    public void updateEvent(Long eventId, UpdateEventRequest request, List<MultipartFile> images) throws IOException {
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
                    (request.getUniversityId().get() != null && request.getUniversityId().get() != 0L ? 
                        universityRepository.findById(request.getUniversityId().get())
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학교를 찾을 수 없습니다.")) 
                        : null) 
                    : event.getUniversity()
        );

        // 새 이미지가 있으면 기존 이미지 삭제 후 새로 업로드
        if (images != null && !images.isEmpty()) {
            // 기존 S3 이미지 삭제
            for (EventImage oldImage : event.getImages()) {
                s3Service.deleteFile(oldImage.getImageUrl());
            }
            event.clearImages();
            
            // 새 이미지 업로드
            uploadAndSaveImages(event, images);
        }
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        // S3 이미지 삭제
        for (EventImage image : event.getImages()) {
            s3Service.deleteFile(image.getImageUrl());
        }

        eventRepository.delete(event);
    }

    private void uploadAndSaveImages(Event event, List<MultipartFile> images) throws IOException {
        if (images == null || images.isEmpty()) {
            return;
        }

        int orderIndex = 0;
        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;

            String imageUrl = s3Service.uploadFile(file);
            EventImage eventImage = EventImage.builder()
                    .imageUrl(imageUrl)
                    .orderIndex(orderIndex++)
                    .build();
            event.addImage(eventImage);
        }
    }
}
