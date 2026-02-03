package com.looky.domain.event.entity;

import com.looky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "events")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(targetClass = EventType.class)
    @CollectionTable(name = "event_types", joinColumns = @JoinColumn(name = "event_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private Set<EventType> eventTypes = new HashSet<>();

    private Double latitude;

    private Double longitude;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventImage> images = new ArrayList<>();

    @Builder
    public Event(String title, String description, Set<EventType> eventTypes, Double latitude, Double longitude, LocalDateTime startDateTime, LocalDateTime endDateTime, EventStatus status) {
        this.title = title;
        this.description = description;
        this.eventTypes = eventTypes != null ? eventTypes : new HashSet<>();
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status != null ? status : EventStatus.UPCOMING;
    }

    public void update(String title, String description, Set<EventType> eventTypes, Double latitude, Double longitude, LocalDateTime startDateTime, LocalDateTime endDateTime, EventStatus status) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (eventTypes != null) {
            this.eventTypes.clear();
            this.eventTypes.addAll(eventTypes);
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
        if (startDateTime != null) {
            this.startDateTime = startDateTime;
        }
        if (endDateTime != null) {
            this.endDateTime = endDateTime;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void addImage(EventImage image) {
        this.images.add(image);
        image.setEvent(this);
    }

    public void clearImages() {
        this.images.clear();
    }
}
