package com.looky.domain.event.repository;

import com.looky.domain.event.entity.Event;
import com.looky.domain.event.entity.EventStatus;
import com.looky.domain.event.entity.EventType;
import jakarta.persistence.criteria.SetJoin;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class EventSpecification {

    public static Specification<Event> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return cb.like(root.get("title"), "%" + keyword + "%");
        };
    }

    public static Specification<Event> hasEventTypes(List<EventType> eventTypes) {
        return (root, query, cb) -> {
            if (eventTypes == null || eventTypes.isEmpty()) {
                return null;
            }
            SetJoin<Event, EventType> typeJoin = root.joinSet("eventTypes");
            query.distinct(true);
            return typeJoin.in(eventTypes);
        };
    }

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Event> hasUniversityId(Long universityId) {
        return (root, query, cb) -> {
            if (universityId == null) {
                return null;
            }
            return cb.equal(root.get("university").get("id"), universityId);
        };
    }
}
