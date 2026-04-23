package com.chitfund.service;

import com.chitfund.dto.*;
import org.springframework.stereotype.Service;

@Service
public class LedgerExportService {

    private final LedgerService ledgerService;

    public LedgerExportService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    public String exportCSV(Long groupId) {

        LedgerFullResponse data = ledgerService.getFullLedger(groupId);

        StringBuilder sb = new StringBuilder();

        // 🔹 Header
        sb.append("Member");
        for (String month : data.getMonths()) {
            sb.append(",").append(month);
        }
        sb.append("\n");

        // 🔹 Rows
        for (MemberLedgerDTO member : data.getMembers()) {

            sb.append(member.getName());

            for (PaymentStatusDTO p : member.getPayments()) {
                sb.append(",").append(p.getPaid() ? "✔" : "❌");
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}