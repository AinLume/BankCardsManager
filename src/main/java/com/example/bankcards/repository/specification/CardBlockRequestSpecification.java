package com.example.bankcards.repository.specification;

import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardBlockRequestStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class CardBlockRequestSpecification {
    public static Specification<CardBlockRequest> hasStatus(CardBlockRequestStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<CardBlockRequest> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<CardBlockRequest> createdBefore(LocalDateTime date) {
        return (root, query, cb) -> date == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), date);
    }
}
