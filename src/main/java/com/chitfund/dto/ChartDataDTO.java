package com.chitfund.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartDataDTO {
    private String month;
    private Double collection;
    private Double profit;
}