package com.chitfund.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
public class MemberLedgerDTO {
    private Long memberId;
    private String name;
    private List<PaymentStatusDTO> payments;
}