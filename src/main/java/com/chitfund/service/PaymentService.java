package com.chitfund.service;

import com.chitfund.entity.Payment;
import com.chitfund.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository repo;
    private final AuditService auditService;

    public PaymentService(PaymentRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }


    public List<Payment> getLedger(Long groupId, LocalDate month) {
        return repo.findByGroupIdAndMonth(groupId, month);
    }

    public Payment collectPayment(Payment payment) {
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (payment.getGroupId() == null || payment.getGroupId() <= 0
                || payment.getMemberId() == null || payment.getMemberId() <= 0
                || payment.getMonth() == null) {
            throw new IllegalArgumentException("Payment group, member, and month are required");
        }

        Optional<Payment> existing =
                repo.findByGroupIdAndMemberIdAndMonth(
                        payment.getGroupId(),
                        payment.getMemberId(),
                        payment.getMonth()
                );

        if (existing.isPresent()) {
            throw new RuntimeException("Payment already exists. Payment history is immutable; create an adjustment record instead of editing an existing payment.");
        }

        Payment saved = repo.save(payment);
        auditService.record(
                "PAYMENT_CREATED",
                "Payment",
                saved.getId(),
                "groupId=" + saved.getGroupId() + ", memberId=" + saved.getMemberId() + ", month=" + saved.getMonth() + ", amount=" + saved.getAmount()
        );
        return saved;
    }
}
