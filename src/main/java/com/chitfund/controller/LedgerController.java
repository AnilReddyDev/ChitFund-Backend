package com.chitfund.controller;

import com.chitfund.dto.LedgerFullResponse;
import com.chitfund.dto.LedgerResponse;
import com.chitfund.service.LedgerExportService;
import com.chitfund.service.LedgerService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
@Validated
public class LedgerController {

    private final LedgerService service;
    private final LedgerExportService exportService;


    public LedgerController(LedgerService service, LedgerExportService exportService) {
        this.service = service;
        this.exportService = exportService;
    }

    @GetMapping("/full")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).VIEW_REPORTS)")
    public LedgerFullResponse getLedger(@RequestParam @Positive Long groupId) {
        return service.getFullLedger(groupId);
    }

    @GetMapping("/export/csv")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).EXPORT_REPORTS)")
    public ResponseEntity<String> exportCSV(@RequestParam @Positive Long groupId) {

        String csv = exportService.exportCSV(groupId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=ledger.csv")
                .header("Content-Type", "text/csv")
                .body(csv);
    }
}
