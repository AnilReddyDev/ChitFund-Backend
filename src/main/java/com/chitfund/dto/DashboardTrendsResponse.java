package com.chitfund.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendsResponse {

    private List<CollectionTrendPoint> collectionTrend;
    private List<ProfitTrendPoint> profitTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionTrendPoint {
        private Integer month;
        private Double expected;
        private Double collected;
        private Double pending;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitTrendPoint {
        private Integer month;
        private Double profit;
    }
}
