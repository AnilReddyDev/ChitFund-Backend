package com.chitfund.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private GroupSummary group;
    private CollectionSummary collection;
    private PaymentsSummary payments;
    private AuctionSummary auction;
    private ProfitSummary profit;
    private HealthSummary health;
    private List<Recommendation> recommendations;
    private MonthSummary monthSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupSummary {
        private Long id;
        private String name;
        private Double totalAmount;
        private Double monthlyPremium;
        private Integer totalMembers;
        private Integer assignedMembers;
        private Integer duration;
        private Integer currentMonth;
        private LocalDate startMonth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionSummary {
        private Double expectedThisMonth;
        private Double collectedThisMonth;
        private Double pendingThisMonth;
        private Integer collectionRate;
        private Double totalCollectedTillNow;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentsSummary {
        private List<PendingMember> pendingMembers;
        private List<OverdueMember> overdueMembers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingMember {
        private Long memberId;
        private String name;
        private String phone;
        private Double amountDue;
        private Integer month;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverdueMember {
        private Long memberId;
        private String name;
        private String phone;
        private List<Integer> missedMonths;
        private Double amountDue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuctionSummary {
        private String currentMonthAuctionStatus;
        private LastAuction lastAuction;
        private NextAuction nextAuction;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastAuction {
        private Long id;
        private Integer month;
        private Long winnerMemberId;
        private String winnerName;
        private Double bidAmount;
        private Double payoutAmount;
        private Double profit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextAuction {
        private Integer month;
        private Integer eligibleMembers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitSummary {
        private Double totalProfit;
        private Double averageProfitPerAuction;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthSummary {
        private String status;
        private Integer score;
        private List<String> reasons;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String type;
        private String priority;
        private String title;
        private String description;
        private String targetRoute;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        private Integer month;
        private Integer paidMembers;
        private Integer pendingMembers;
        private Integer overdueMembers;
        private Double expectedAmount;
        private Double collectedAmount;
        private Double pendingAmount;
        private String auctionStatus;
        private Integer eligibleAuctionMembers;
    }
}
