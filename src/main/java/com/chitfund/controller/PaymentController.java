package com.chitfund.controller;

import com.chitfund.entity.Payment;
import com.chitfund.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Payment collect(@Valid @RequestBody Payment payment) {
        return service.collectPayment(payment);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<Payment> ledger(
            @RequestParam @Positive Long groupId,
            @RequestParam @NotNull LocalDate month
    ) {
        return service.getLedger(groupId, month);
    }
}
