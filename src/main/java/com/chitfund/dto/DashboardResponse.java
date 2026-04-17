package com.chitfund.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class DashboardResponse {

    private Double totalCollection;
    private Double totalProfit;
    private Integer totalMembers;
    private Integer pendingPayments;
    private Integer currentMonth;
    private String lastWinner;
}