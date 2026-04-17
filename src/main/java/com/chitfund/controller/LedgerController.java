package com.chitfund.controller;

import com.chitfund.dto.LedgerFullResponse;
import com.chitfund.dto.LedgerResponse;
import com.chitfund.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService service;

    public LedgerController(LedgerService service) {
        this.service = service;
    }

    @GetMapping("/full")
    public LedgerFullResponse getLedger(@RequestParam Long groupId) {
        return service.getFullLedger(groupId);
    }
}