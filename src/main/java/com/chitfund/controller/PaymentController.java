package com.chitfund.controller;

import com.chitfund.entity.Payment;
import com.chitfund.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public Payment collect(@RequestBody Payment payment) {
        return service.collectPayment(payment);
    }

    @GetMapping
    public List<Payment> ledger(
            @RequestParam Long groupId,
            @RequestParam LocalDate month
    ) {
        return service.getLedger(groupId, month);
    }
}