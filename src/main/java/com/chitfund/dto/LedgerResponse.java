// dto/LedgerResponse.java
package com.chitfund.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class LedgerResponse {
    private Long memberId;
    private String name;
    private Boolean paid;
}