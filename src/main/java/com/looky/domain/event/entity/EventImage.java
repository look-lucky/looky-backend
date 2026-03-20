package com.looky.domain.event.entity;

import com.looky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_image")
public class EventImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String imageUrl;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'GENERAL'")
    private EventImageType imageType;

    @Builder
    public EventImage(String imageUrl, Integer orderIndex, EventImageType imageType) {
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
        this.imageType = imageType != null ? imageType : EventImageType.GENERAL;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void updateOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
