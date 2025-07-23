package com.example.bankcards.repository.specification;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CardSpecification {
    public static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Card> expiryDateAfter(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("expiryDate"), date);
    }

    public static Specification<Card> expiryDateBefore(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.lessThanOrEqualTo(root.get("expiryDate"), date);
    }

    public static Specification<Card> balanceMoreThan(Long amount) {
        return (root, query, cb) -> amount == null ? null : cb.greaterThanOrEqualTo(root.get("balance"), amount);
    }

    public static Specification<Card> balanceLessThan(Long amount) {
        return (root, query, cb) -> amount == null ? null : cb.lessThanOrEqualTo(root.get("balance"), amount);
    }

    public static Specification<Card> belongsToUser(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }
}
