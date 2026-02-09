package com.looky.domain.event.entity;
import com.looky.domain.organization.entity.University;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

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
    public Event(String title, String description, Set<EventType> eventTypes, Double latitude, Double longitude, LocalDateTime startDateTime, LocalDateTime endDateTime, EventStatus status, University university) {
        this.title = title;
        this.description = description;
        this.eventTypes = eventTypes != null ? eventTypes : new HashSet<>();
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status != null ? status : EventStatus.UPCOMING;
        this.university = university;
    }

    public void update(String title, String description, Set<EventType> eventTypes, Double latitude, Double longitude, LocalDateTime startDateTime, LocalDateTime endDateTime, EventStatus status) {
        this.title = title;
        this.description = description;
        
        if (eventTypes != null) {
            this.eventTypes.clear(); // JPA 영속성 컨텍스트 유지를 위해 컬렉션 전체 교체 대신 내용물 교체
            this.eventTypes.addAll(eventTypes);
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status;
    }

    public void addImage(EventImage image) {
        this.images.add(image);
        image.setEvent(this);
    }

    public void clearImages() {
        this.images.clear();
    }
}
