package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardBlockRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequest, Long>,
        JpaSpecificationExecutor<CardBlockRequest> {

    List<CardBlockRequest> findByUserId(Long userId);

    boolean existsByCardAndStatus(Card card, CardBlockRequestStatus status);
}
