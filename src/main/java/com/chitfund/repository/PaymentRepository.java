package com.chitfund.repository;

import com.chitfund.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByGroupIdAndMonth(Long groupId, LocalDate month);


    Optional<Payment> findByGroupIdAndMemberIdAndMonth(
            Long groupId,
            Long memberId,
            LocalDate month
    );

    List<Payment> findByGroupId(Long groupId);
}