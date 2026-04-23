package com.chitfund.controller;

import com.chitfund.dto.LedgerFullResponse;
import com.chitfund.dto.LedgerResponse;
import com.chitfund.service.LedgerExportService;
import com.chitfund.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService service;
    private final LedgerExportService exportService;


    public LedgerController(LedgerService service, LedgerExportService exportService) {
        this.service = service;
        this.exportService = exportService;
    }

    @GetMapping("/full")
    public LedgerFullResponse getLedger(@RequestParam Long groupId) {
        return service.getFullLedger(groupId);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCSV(@RequestParam Long groupId) {

        String csv = exportService.exportCSV(groupId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=ledger.csv")
                .header("Content-Type", "text/csv")
                .body(csv);
    }
}