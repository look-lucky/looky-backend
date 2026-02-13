package com.looky.domain.event.dto;

import com.looky.domain.event.entity.Event;
import com.looky.domain.event.entity.EventImage;
import com.looky.domain.event.entity.EventStatus;
import com.looky.domain.event.entity.EventType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private Set<EventType> eventTypes;
    private Double latitude;
    private Double longitude;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String place;
    private EventStatus status;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    private EventResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.eventTypes = event.getEventTypes();
        this.latitude = event.getLatitude();
        this.longitude = event.getLongitude();
        this.startDateTime = event.getStartDateTime();
        this.endDateTime = event.getEndDateTime();
        this.place = event.getPlace();
        this.status = event.getStatus();
        this.imageUrls = event.getImages().stream()
                .map(EventImage::getImageUrl)
                .collect(Collectors.toList());
        this.createdAt = event.getCreatedAt();
    }

    public static EventResponse from(Event event) {
        return new EventResponse(event);
    }
}
