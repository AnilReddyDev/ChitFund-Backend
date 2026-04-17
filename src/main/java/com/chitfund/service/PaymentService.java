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

    public PaymentService(PaymentRepository repo) {
        this.repo = repo;
    }


    public List<Payment> getLedger(Long groupId, LocalDate month) {
        return repo.findByGroupIdAndMonth(groupId, month);
    }

    public Payment collectPayment(Payment payment) {

        Optional<Payment> existing =
                repo.findByGroupIdAndMemberIdAndMonth(
                        payment.getGroupId(),
                        payment.getMemberId(),
                        payment.getMonth()
                );

        if (existing.isPresent()) {
            throw new RuntimeException("Payment already exists");
        }

        return repo.save(payment);
    }
}