package com.chitfund.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
public class LedgerFullResponse {
    private List<String> months;
    private List<MemberLedgerDTO> members;
}