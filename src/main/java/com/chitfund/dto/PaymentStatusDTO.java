package com.chitfund.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class PaymentStatusDTO {
    private String month;
    private Boolean paid;
}