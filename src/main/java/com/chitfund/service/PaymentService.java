package com.chitfund.service;

import com.chitfund.audit.Auditable;
import com.chitfund.entity.AuditAction;
import com.chitfund.entity.Payment;
import com.chitfund.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository repo;

    public PaymentService(PaymentRepository repo) {
        this.repo = repo;
    }


    public List<Payment> getLedger(Long groupId, LocalDate month) {
        return repo.findByGroupIdAndMonth(groupId, month);
    }

    @Auditable(action = AuditAction.CREATE, entityType = "Payment", entityClass = Payment.class)
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

        return repo.save(payment);
    }
}
